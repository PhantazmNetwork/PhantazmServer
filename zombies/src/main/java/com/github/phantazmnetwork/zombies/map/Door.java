package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.core.hologram.Hologram;
import com.github.phantazmnetwork.core.hologram.InstanceHologram;
import com.github.phantazmnetwork.zombies.map.action.Action;
import com.github.steanky.vector.*;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a door. May be opened once.
 */
public class Door {
    private static final Logger LOGGER = LoggerFactory.getLogger(Door.class);
    private static final Bounds3I[] EMPTY_BOUNDS_ARRAY = new Bounds3I[0];

    private final Instance instance;
    private final DoorInfo doorInfo;
    private final Block fillBlock;
    private final Bounds3I enclosing;
    private final Vec3D center;
    private final List<Bounds3I> regions;

    private final ArrayList<Hologram> holograms;
    private final List<Action<Door>> openActions;

    private final Vec3I2ObjectMap<Block> blockMappings;

    private boolean isOpen;

    /**
     * Constructs a new instance of this class.
     *
     * @param doorInfo the backing data object
     * @param instance the instance which this MapObject is in
     */
    public Door(@NotNull Vec3I mapOrigin, @NotNull DoorInfo doorInfo, @NotNull Instance instance,
            @NotNull Block fillBlock, @NotNull List<Action<Door>> openActions) {
        Vec3I origin = mapOrigin.add(
                Bounds3I.enclosingImmutable(doorInfo.regions().toArray(EMPTY_BOUNDS_ARRAY)).immutableCenter()
                        .floorToImmutableInt());
        this.instance = Objects.requireNonNull(instance, "instance");
        this.doorInfo = Objects.requireNonNull(doorInfo, "doorInfo");
        this.fillBlock = Objects.requireNonNull(fillBlock, "fillBlock");

        List<Bounds3I> regions = doorInfo.regions();
        if (regions.isEmpty()) {
            LOGGER.warn("Door has no regions, enclosing bounds and center set to origin");

            enclosing = Bounds3I.immutable(origin, 1, 1, 1);
            center = origin.toMutableDouble().add(0.5, 0.5, 0.5).immutable();
            this.regions = Collections.emptyList();
        }
        else {
            Bounds3I[] regionArray = regions.toArray(EMPTY_BOUNDS_ARRAY);
            for (int i = 0; i < regionArray.length; i++) {
                regionArray[i] = regionArray[i].shift(origin);
            }

            enclosing = Bounds3I.enclosingImmutable(regionArray);
            center = enclosing.immutableCenter();
            this.regions = List.of(regionArray);
        }

        List<HologramInfo> hologramInfo = doorInfo.holograms();
        holograms = new ArrayList<>(hologramInfo.size());

        initHolograms(hologramInfo);

        this.openActions = List.copyOf(openActions);
        this.blockMappings = new HashVec3I2ObjectMap<>(enclosing.originX(), enclosing.originX(), enclosing.originZ(),
                enclosing.lengthX(), enclosing.lengthY(), enclosing.lengthZ());
    }

    private void initHolograms(List<HologramInfo> hologramInfo) {
        for (HologramInfo info : hologramInfo) {
            Vec3D offset = info.position();
            Hologram hologram = new InstanceHologram(
                    Vec3D.immutable(center.x() + offset.x(), center.y() + offset.y(), center.z() + offset.z()), 0.1);
            hologram.addAll(info.text());
            hologram.setInstance(instance);
            holograms.add(hologram);
        }
    }

    /**
     * Determines if this door is currently open or not.
     *
     * @return true if this door has been opened, false otherwise
     */
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * Opens this door, removing its blocks. If the door is already open, this method will do nothing.
     */
    public void open() {
        if (isOpen) {
            return;
        }

        isOpen = true;

        for (Bounds3I region : regions) {
            region.forEach((x, y, z) -> {
                Block oldBlock = instance.getBlock(x, y, z);
                blockMappings.put(x, y, z, oldBlock);
                instance.setBlock(x, y, z, fillBlock);
            });
        }

        for (Hologram hologram : holograms) {
            hologram.clear();
            hologram.trimToSize();
        }

        holograms.clear();
        holograms.trimToSize();

        for (Action<Door> action : openActions) {
            action.perform(this);
        }
    }

    /**
     * Closes this door. Has no effect if the door is already closed.
     */
    public void close() {
        if (!isOpen) {
            return;
        }

        isOpen = false;
        blockMappings.forEach((x, y, z, block) -> instance.setBlock(x, y, z, block));

        initHolograms(doorInfo.holograms());
        blockMappings.clear();
    }

    public @Unmodifiable @NotNull List<Bounds3I> regions() {
        return regions;
    }

    /**
     * Gets the center of the door, to which distances should be measured. This is equal to the center of the enclosing
     * region.
     *
     * @return the center of the door
     */
    public @NotNull Vec3D getCenter() {
        return center;
    }

    /**
     * Gets the enclosing region of this door, which is the smallest possible bounding box that encloses every
     * subregion for this door.
     *
     * @return the enclosing region for this door
     */
    public @NotNull Bounds3I getEnclosing() {
        return enclosing;
    }
}
