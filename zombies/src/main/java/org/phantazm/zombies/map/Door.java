package org.phantazm.zombies.map;

import com.github.steanky.vector.Bounds3I;
import com.github.steanky.vector.HashVec3I2ObjectMap;
import com.github.steanky.vector.Vec3D;
import com.github.steanky.vector.Vec3I2ObjectMap;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.VecUtils;
import org.phantazm.core.hologram.Hologram;
import org.phantazm.core.hologram.InstanceHologram;
import org.phantazm.core.tracker.BoundedBase;
import org.phantazm.zombies.map.action.Action;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Represents a door. May be opened and closed.
 */
public class Door extends BoundedBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(Door.class);

    private final Instance instance;
    private final DoorInfo doorInfo;
    private final Block fillBlock;
    private final Bounds3I enclosing;
    private final List<Bounds3I> regions;

    private final ArrayList<Hologram> holograms;
    private final List<Action<Door>> openActions;
    private final List<Action<Door>> closeActions;
    private final List<Action<Door>> failOpenActions;

    private final Supplier<? extends MapObjects> mapObjects;

    private final Vec3I2ObjectMap<Block> blockMappings;

    private volatile boolean isOpen;

    private volatile ZombiesPlayer lastInteractor;

    private final Object sync;

    /**
     * Constructs a new instance of this class.
     *
     * @param doorInfo the backing data object
     * @param instance the instance which this MapObject is in
     */
    public Door(@NotNull Point mapOrigin, @NotNull DoorInfo doorInfo, @NotNull Instance instance,
            @NotNull Block fillBlock, @NotNull List<Action<Door>> openActions, @NotNull List<Action<Door>> closeActions,
            @NotNull List<Action<Door>> failOpenActions, @NotNull Supplier<? extends MapObjects> mapObjects) {
        super(mapOrigin, doorInfo.regions());

        this.instance = Objects.requireNonNull(instance, "instance");
        this.doorInfo = Objects.requireNonNull(doorInfo, "doorInfo");
        this.fillBlock = Objects.requireNonNull(fillBlock, "fillBlock");

        List<Bounds3I> regions = doorInfo.regions();
        if (regions.isEmpty()) {
            LOGGER.warn("Door has no regions, enclosing bounds and center set to map origin");

            enclosing = Bounds3I.immutable(mapOrigin.blockX(), mapOrigin.blockY(), mapOrigin.blockZ(), 1, 1, 1);
            this.regions = List.of();
        }
        else {
            Bounds3I[] regionArray = doorInfo.regions().toArray(Bounds3I[]::new);
            for (int i = 0; i < regionArray.length; i++) {
                regionArray[i] = regionArray[i].shift(mapOrigin.blockX(), mapOrigin.blockY(), mapOrigin.blockZ());
            }

            enclosing = Bounds3I.enclosingImmutable(regionArray);
            this.regions = List.of(regionArray);
        }

        List<HologramInfo> hologramInfo = doorInfo.holograms();
        holograms = new ArrayList<>(hologramInfo.size());

        initHolograms(hologramInfo);

        this.openActions = List.copyOf(openActions);
        this.closeActions = List.copyOf(closeActions);
        this.failOpenActions = List.copyOf(failOpenActions);
        this.mapObjects = Objects.requireNonNull(mapObjects, "mapObjects");
        this.blockMappings = new HashVec3I2ObjectMap<>(enclosing.originX(), enclosing.originX(), enclosing.originZ(),
                enclosing.lengthX(), enclosing.lengthY(), enclosing.lengthZ());

        this.sync = new Object();
    }

    private void initHolograms(List<HologramInfo> hologramInfo) {
        for (HologramInfo info : hologramInfo) {
            Vec3D offset = info.position();
            Hologram hologram = new InstanceHologram(center.add(VecUtils.toPoint(offset)), 0.1);
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

    private void removeBlocksAndHolograms() {
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
    }

    /**
     * Opens this door, removing its blocks. If the door is already open, this method will do nothing.
     */
    public void open(@Nullable ZombiesPlayer interactor) {
        synchronized (sync) {
            if (isOpen) {
                return;
            }

            this.lastInteractor = interactor;
            isOpen = true;

            removeBlocksAndHolograms();

            instance.playSound(doorInfo.openSound(), center.x(), center.y(), center.z());

            for (Action<Door> action : openActions) {
                action.perform(this);
            }

            for (Key key : doorInfo.opensTo()) {
                Room room = mapObjects.get().roomMap().get(key);
                if (room != null) {
                    if (room.isOpen()) {
                        continue;
                    }

                    room.open();

                    for (Door otherDoor : mapObjects.get().doorTracker().items()) {
                        if (otherDoor == this || otherDoor.isOpen) {
                            continue;
                        }

                        boolean allOpen = true;
                        for (Key otherRoomKey : otherDoor.doorInfo.opensTo()) {
                            Room otherRoom = mapObjects.get().roomMap().get(otherRoomKey);
                            if (otherRoom != null && !otherRoom.isOpen()) {
                                allOpen = false;
                                break;
                            }
                        }

                        if (allOpen) {
                            synchronized (otherDoor.sync) {
                                otherDoor.isOpen = false;
                                otherDoor.removeBlocksAndHolograms();
                            }
                        }
                    }
                }
                else {
                    LOGGER.warn("Tried to open nonexistent room " + key);
                }
            }
        }
    }

    public void failOpen(@Nullable ZombiesPlayer zombiesPlayer) {
        synchronized (sync) {
            if (isOpen) {
                return;
            }

            this.lastInteractor = zombiesPlayer;

            for (Action<Door> failOpenAction : failOpenActions) {
                failOpenAction.perform(this);
            }
        }
    }

    /**
     * Closes this door. Has no effect if the door is already closed.
     */
    public void close(@Nullable ZombiesPlayer interactor) {
        synchronized (sync) {
            if (!isOpen) {
                return;
            }

            this.lastInteractor = interactor;
            isOpen = false;
            blockMappings.forEach((x, y, z, block) -> instance.setBlock(x, y, z, block));

            initHolograms(doorInfo.holograms());
            blockMappings.clear();

            for (Action<Door> closeAction : closeActions) {
                closeAction.perform(this);
            }
        }
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

    public @NotNull DoorInfo doorInfo() {
        return doorInfo;
    }

    public @NotNull Optional<ZombiesPlayer> lastInteractor() {
        return Optional.ofNullable(lastInteractor);
    }

    @Override
    public @NotNull @Unmodifiable List<Bounds3I> bounds() {
        return regions;
    }

    @Override
    public @NotNull Point center() {
        return center;
    }
}
