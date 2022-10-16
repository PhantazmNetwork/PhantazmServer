package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.commons.vector.Region3I;
import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.core.hologram.Hologram;
import com.github.phantazmnetwork.core.hologram.InstanceHologram;
import com.github.phantazmnetwork.zombies.game.map.action.Action;
import com.github.phantazmnetwork.zombies.map.DoorInfo;
import com.github.phantazmnetwork.zombies.map.HologramInfo;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Represents a door. May be opened once.
 */
public class Door extends PositionalMapObject<DoorInfo> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Door.class);
    private static final Region3I[] EMPTY_REGION_ARRAY = new Region3I[0];

    private final Block fillBlock;
    private final Region3I enclosing;
    private final Vec3D center;
    private final List<Region3I> regions;

    private final ArrayList<Hologram> holograms;
    private final List<Action<Door>> openActions;

    private final Map<Vec3I, Block> blockMappings;

    private boolean isOpen;

    /**
     * Constructs a new instance of this class.
     *
     * @param doorInfo the backing data object
     * @param instance the instance which this MapObject is in
     */
    public Door(@NotNull DoorInfo doorInfo, @NotNull Instance instance, @NotNull Block fillBlock,
            @NotNull List<Action<Door>> openActions) {
        super(doorInfo, Vec3I.floored(Region3I.enclosing(doorInfo.regions().toArray(EMPTY_REGION_ARRAY)).getCenter()),
                instance);
        this.fillBlock = Objects.requireNonNull(fillBlock, "fillBlock");

        List<Region3I> regions = doorInfo.regions();
        if (regions.size() == 0) {
            LOGGER.warn("Door has no regions, enclosing bounds and center set to origin");

            enclosing = Region3I.encompassing(origin, origin);
            center = Vec3D.of(origin);
            this.regions = Collections.emptyList();
        }
        else {
            Region3I[] regionArray = regions.toArray(EMPTY_REGION_ARRAY);
            for (int i = 0; i < regionArray.length; i++) {
                regionArray[i] = regionArray[i].add(origin);
            }

            enclosing = Region3I.enclosing(regionArray);
            center = enclosing.getCenter();
            this.regions = List.of(regionArray);
        }

        List<HologramInfo> hologramInfo = data.holograms();
        holograms = new ArrayList<>(hologramInfo.size());

        initHolograms(hologramInfo);

        this.openActions = List.copyOf(openActions);
        this.blockMappings = new HashMap<>();
    }

    private void initHolograms(List<HologramInfo> hologramInfo) {
        for (HologramInfo info : hologramInfo) {
            Vec3D offset = info.position();
            Hologram hologram = new InstanceHologram(
                    Vec3D.of(center.getX() + offset.getX(), center.getY() + offset.getY(),
                            center.getZ() + offset.getZ()), 0.1);
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

        for (Region3I region : regions) {
            for (Vec3I pos : region) {
                Block oldBlock = instance.getBlock(pos.getX(), pos.getY(), pos.getZ());
                blockMappings.put(pos, oldBlock);
                instance.setBlock(pos.getX(), pos.getY(), pos.getZ(), fillBlock);
            }
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
        for (Map.Entry<Vec3I, Block> blockEntry : blockMappings.entrySet()) {
            Vec3I pos = blockEntry.getKey();
            instance.setBlock(pos.getX(), pos.getY(), pos.getZ(), blockEntry.getValue());
        }

        initHolograms(data.holograms());
        blockMappings.clear();
    }

    public @Unmodifiable @NotNull List<Region3I> regions() {
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
    public @NotNull Region3I getEnclosing() {
        return enclosing;
    }
}
