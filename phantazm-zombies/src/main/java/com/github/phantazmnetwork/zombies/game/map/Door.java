package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.api.hologram.Hologram;
import com.github.phantazmnetwork.api.hologram.InstanceHologram;
import com.github.phantazmnetwork.commons.vector.Region3I;
import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.zombies.map.DoorInfo;
import com.github.phantazmnetwork.zombies.map.HologramInfo;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a door. May be opened once.
 */
public class Door extends PositionalMapObject<DoorInfo> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Door.class);
    private final Block fillBlock;
    private final Region3I enclosing;
    private final Vec3D center;
    private final ArrayList<Hologram> holograms;

    private boolean isOpen;

    /**
     * Constructs a new instance of this class.
     *
     * @param doorInfo  the backing data object
     * @param origin the origin vector to which this door's coordinates are considered relative
     * @param instance  the instance which this MapObject is in
     */
    public Door(@NotNull DoorInfo doorInfo, @NotNull Vec3I origin, @NotNull Instance instance,
                @NotNull Block fillBlock) {
        super(doorInfo, origin, instance);
        this.fillBlock = Objects.requireNonNull(fillBlock, "fillBlock");

        List<Region3I> regions = doorInfo.regions();
        if(regions.size() == 0) {
            LOGGER.warn("Door has no regions, enclosing bounds and center set to origin");
            enclosing = Region3I.encompassing(origin, origin);
            center = Vec3D.of(origin);
        }
        else {
            Region3I[] regionArray = regions.toArray(new Region3I[0]);
            for(int i = 0; i < regionArray.length; i++) {
                Region3I current = regionArray[i];
                regionArray[i] = current.add(origin);
            }

            enclosing = Region3I.enclosing(regionArray);
            center = enclosing.getCenter();
        }

        List<HologramInfo> hologramInfo = data.holograms();
        holograms = new ArrayList<>(hologramInfo.size());

        for(HologramInfo info : hologramInfo) {
            Vec3D offset = info.position();
            Hologram hologram = new InstanceHologram(Vec3D.of(center.getX() + offset.getX(), center.getY() +
                    offset.getY(), center.getZ() + offset.getZ()), 0.1);
            hologram.addAll(info.text());
            hologram.setInstance(instance);
            holograms.add(hologram);
        }
    }

    /**
     * Determines if this door is currently open or not.
     * @return true if this door has been opened, false otherwise
     */
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * Permanently opens this door, removing its blocks. If the door is already open, this method will do nothing.
     */
    public void open() {
        if(!isOpen) {
            isOpen = true;

            for(Region3I region : data.regions()) {
                for(Vec3I block : region) {
                    instance.setBlock(block.getX() + origin.getX(), block.getY() + origin.getY(), block
                            .getZ() + origin.getZ(), fillBlock);
                }
            }

            for(Hologram hologram : holograms) {
                hologram.clear();
                hologram.trimToSize();
            }

            holograms.clear();
            holograms.trimToSize();
        }
    }

    /**
     * Gets the center of the door, to which distances should be measured. This is equal to the center of the enclosing
     * region.
     * @return the center of the door
     */
    public @NotNull Vec3D getCenter() {
        return center;
    }

    /**
     * Gets the enclosing region of this door, which is the smallest possible bounding box that encloses every
     * subregion for this door.
     * @return the enclosing region for this door
     */
    public @NotNull Region3I getEnclosing() {
        return enclosing;
    }
}
