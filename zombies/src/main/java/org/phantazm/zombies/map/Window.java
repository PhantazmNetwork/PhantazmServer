package org.phantazm.zombies.map;

import com.github.steanky.vector.Bounds3I;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.ClientBlockHandler;
import org.phantazm.core.tracker.BoundedBase;
import org.phantazm.core.tracker.BoundedTracker;
import org.phantazm.zombies.map.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a window in-game. May be repaired or broken. Broken window blocks are replaced by air on the server-side,
 * but are considered barriers by clients.
 *
 * @see ClientBlockHandler
 */
public class Window extends BoundedBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(Window.class);

    private final Instance instance;
    private final WindowInfo windowInfo;
    private final ClientBlockHandler clientBlockHandler;
    private final Point worldMin;
    private final int volume;
    private final ArrayList<Block> repairBlocks;

    private final List<Action<Window>> repairActions;
    private final List<Action<Window>> breakActions;

    private final Room linkedRoom;

    private final Object sync;

    private volatile int index;
    private volatile long lastBreakTime;

    /**
     * Creates a new (fully-repaired) window.
     *
     * @param instance           the instance in which the window is present
     * @param windowInfo         the data defining the configurable parameters of this window
     * @param clientBlockHandler the {@link ClientBlockHandler} used to set client-only barrier blocks
     */
    public Window(@NotNull Point mapOrigin, @NotNull Instance instance, @NotNull WindowInfo windowInfo,
            @NotNull ClientBlockHandler clientBlockHandler, @NotNull List<Action<Window>> repairActions,
            @NotNull List<Action<Window>> breakActions, @NotNull BoundedTracker<Room> roomTracker) {
        super(mapOrigin, windowInfo.frameRegion());

        this.instance = Objects.requireNonNull(instance, "instance");
        this.windowInfo = Objects.requireNonNull(windowInfo, "data");
        this.clientBlockHandler = Objects.requireNonNull(clientBlockHandler, "clientBlockHandler");

        this.repairActions = List.copyOf(repairActions);
        this.breakActions = List.copyOf(breakActions);

        this.sync = new Object();

        Bounds3I frame = windowInfo.frameRegion();
        worldMin = mapOrigin.add(frame.originX(), frame.originY(), frame.originZ());
        volume = frame.volume();

        if (volume == 0) {
            throw new IllegalArgumentException("Zero-volume window");
        }

        Optional<Room> closestRoom = roomTracker.closestInRangeToBounds(center, 10);
        if (closestRoom.isEmpty()) {
            this.linkedRoom = null;
            LOGGER.warn("No linkable room found within 10 blocks for window at ~" + center);
            LOGGER.warn("If any spawnpoints link to this window, they will never be able to spawn.");
        }
        else {
            this.linkedRoom = closestRoom.get();
        }

        repairBlocks = new ArrayList<>(volume);
        super.bounds.get(0).forEach((x, y, z) -> {
            Block block = instance.getBlock(x, y, z, Block.Getter.Condition.TYPE);
            if (block == null) {
                block = Block.AIR;
            }

            repairBlocks.add(block);
        });

        this.index = this.volume;
    }

    public @NotNull WindowInfo getWindowInfo() {
        return windowInfo;
    }

    /**
     * Checks if the given {@link Point} is within the distance {@code range} specifies to the center of this window.
     *
     * @param point the point to check
     * @param range the distance this point should be considered "in range" of this window
     * @return true if the point is in range, false otherwise
     */
    public boolean isInRange(@NotNull Point point, double range) {
        Objects.requireNonNull(point, "point");
        return point.distanceSquared(center) < range * range;
    }

    /**
     * Updates the window index, playing the appropriate effects depending on if the index increased (repair) or
     * decreased (break). Setting {@code newIndex} equal to this window's volume will fully repair it. Setting
     * {@code newIndex} equal to 0 will fully break it.
     *
     * @param newIndex the new break index
     * @return a positive number indicating the actual number of repaired blocks, or a negative number indicating the
     * actual number of broken blocks
     */
    public int updateIndex(int newIndex) {
        synchronized (sync) {
            if (newIndex < 0) {
                newIndex = 0;
            }

            newIndex = Math.min(newIndex, volume);

            if (newIndex == index) {
                return 0; //no change
            }

            if (newIndex < index) {
                //play the break sound
                instance.playSound(newIndex == 0 ? windowInfo.breakAllSound() : windowInfo.breakSound(), center);

                for (int i = index - 1; i >= newIndex; i--) {
                    Point breakLocation = indexToCoordinate(i);
                    instance.setBlock(breakLocation, Block.AIR);
                    clientBlockHandler.setClientBlock(Block.BARRIER, breakLocation);
                }

                for (Action<Window> breakAction : breakActions) {
                    breakAction.perform(this);
                }
            }
            else {
                //play the repair sound
                instance.playSound(newIndex == volume ? windowInfo.repairAllSound() : windowInfo.repairSound(), center);

                for (int i = index; i < newIndex; i++) {
                    Point repairLocation = indexToCoordinate(i);
                    instance.setBlock(repairLocation, repairBlocks.get(i));
                    clientBlockHandler.removeClientBlock(repairLocation);
                }

                for (Action<Window> repairAction : repairActions) {
                    repairAction.perform(this);
                }
            }

            int oldIndex = index;
            index = newIndex;

            return newIndex - oldIndex;
        }
    }

    /**
     * Checks if this window is fully repaired or not. Equivalent to {@code getRepairIndex() == getVolume()}.
     *
     * @return true if this window has been fully repaired, false otherwise
     */
    public boolean isFullyRepaired() {
        return index == volume;
    }

    /**
     * Checks if this window is fully broken or not. Equivalent to {@code getRepairIndex() == 0}.
     *
     * @return true if this window has been fully repaired, false otherwise
     */
    public boolean isFullyBroken() {
        return index == 0;
    }

    /**
     * Returns the current "repair index". This is equal to 0 for a fully broken window and {@code getVolume()} for
     * a fully repaired window.
     *
     * @return the current repair index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets the number of blocks in the window face.
     *
     * @return the number of blocks in the window face
     */
    public int getVolume() {
        return volume;
    }

    /**
     * Gets the center of the window frame region in world coordinates. This is the point from which distance to the
     * window should be measured.
     *
     * @return the center of the window
     */
    public @NotNull Point getCenter() {
        return center;
    }

    public void setLastBreakTime(long time) {
        this.lastBreakTime = time;
    }

    public long getLastBreakTime() {
        return lastBreakTime;
    }

    public @NotNull Optional<Room> getLinkedRoom() {
        return Optional.ofNullable(linkedRoom);
    }

    /**
     * Checks if the block is currently broken. Accepts world coordinates. The behavior of this function is undefined
     * if the position specified is outside of the bounds of this window.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param z the z-coordinate
     * @return true if the block is broken, false otherwise
     */
    public boolean isBlockBroken(int x, int y, int z) {
        int index = coordinateToIndex(x, y, z);

        synchronized (sync) {
            return index >= this.index;
        }
    }

    private Point indexToCoordinate(int index) {
        Bounds3I frameRegion = windowInfo.frameRegion();

        int x;
        int y;
        int z;
        if (frameRegion.lengthZ() > frameRegion.lengthX()) {
            x = (index / (frameRegion.lengthZ() * frameRegion.lengthY())) % frameRegion.lengthX();
            y = (index / frameRegion.lengthZ()) % frameRegion.lengthY();
            z = index % frameRegion.lengthZ();
        }
        else {
            x = index % frameRegion.lengthX();
            y = (index / frameRegion.lengthX()) % frameRegion.lengthY();
            z = (index / (frameRegion.lengthX() * frameRegion.lengthY())) % frameRegion.lengthZ();
        }

        return worldMin.add(x, y, z);
    }

    private int coordinateToIndex(int x, int y, int z) {
        int frameRelativeX = x - worldMin.blockX();
        int frameRelativeY = y - worldMin.blockY();
        int frameRelativeZ = z - worldMin.blockZ();

        Bounds3I frameRegion = windowInfo.frameRegion();
        if (frameRegion.lengthZ() > frameRegion.lengthX()) {
            return frameRelativeZ + (frameRelativeY * frameRegion.lengthZ()) +
                    (frameRelativeX * frameRegion.lengthZ() * frameRegion.lengthY());
        }

        return frameRelativeX + (frameRelativeY * frameRegion.lengthX()) +
                (frameRelativeZ * frameRegion.lengthX() * frameRegion.lengthY());
    }
}
