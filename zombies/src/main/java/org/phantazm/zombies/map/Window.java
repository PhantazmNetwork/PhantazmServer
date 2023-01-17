package org.phantazm.zombies.map;

import com.github.steanky.vector.Bounds3I;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;
import org.phantazm.core.ClientBlockHandler;
import org.phantazm.core.VecUtils;
import org.phantazm.zombies.map.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.*;

/**
 * Represents a window in-game. May be repaired or broken. Broken window blocks are replaced by air on the server-side,
 * but are considered barriers by clients.
 *
 * @see ClientBlockHandler
 */
public class Window {
    private static final Logger LOGGER = LoggerFactory.getLogger(Window.class);
    private static final Block DEFAULT_PADDING = Block.OAK_SLAB;

    private final Instance instance;
    private final WindowInfo windowInfo;
    private final ClientBlockHandler clientBlockHandler;
    private final Point worldMin;
    private final Point center;
    private final int volume;
    private final ArrayList<Block> repairBlocks;

    private final List<Action<Window>> repairActions;
    private final List<Action<Window>> breakActions;

    private int index;

    /**
     * Creates a new (fully-repaired) window.
     *
     * @param instance           the instance in which the window is present
     * @param windowInfo         the data defining the configurable parameters of this window
     * @param clientBlockHandler the {@link ClientBlockHandler} used to set client-only barrier blocks
     */
    public Window(@NotNull Point mapOrigin, @NotNull Instance instance, @NotNull WindowInfo windowInfo,
            @NotNull ClientBlockHandler clientBlockHandler, @NotNull List<Action<Window>> repairActions,
            @NotNull List<Action<Window>> breakActions) {
        Bounds3I frameRegion = windowInfo.frameRegion();

        this.instance = Objects.requireNonNull(instance, "instance");
        this.windowInfo = Objects.requireNonNull(windowInfo, "data");
        this.clientBlockHandler = Objects.requireNonNull(clientBlockHandler, "clientBlockTracker");

        this.repairActions = List.copyOf(repairActions);
        this.breakActions = List.copyOf(breakActions);

        Bounds3I frame = windowInfo.frameRegion();

        worldMin = mapOrigin.add(frameRegion.originX(), frameRegion.originY(), frameRegion.originZ());
        center = VecUtils.toPoint(frame.immutableCenter()).add(mapOrigin);
        volume = frame.volume();

        if (volume == 0) {
            throw new IllegalArgumentException("Zero-volume window");
        }

        List<String> repairBlockSnbts = windowInfo.repairBlocks();
        repairBlocks = new ArrayList<>(repairBlockSnbts.size());
        for (String blockString : repairBlockSnbts) {
            try {
                NBTCompound compound = (NBTCompound)new SNBTParser(new StringReader(blockString)).parse();
                String id = compound.getString("Name");
                if (id == null) {
                    LOGGER.warn("Malformed block SNBT " + compound + ", no Name tag found in block data for window " +
                            "at ~" + center);
                    return;
                }

                Block block = Block.fromNamespaceId(id);
                if (block == null) {
                    LOGGER.warn("Found a block with unknown id " + id + " in window at ~" + center);
                    return;
                }

                NBTCompound properties = compound.getCompound("Properties");

                if (properties != null) {
                    Map<String, NBT> propertiesMap = properties.asMapView();
                    Map<String, String> stringMap = new HashMap<>(propertiesMap.size());

                    for (Map.Entry<String, NBT> entry : properties.getEntries()) {
                        NBT nbt = entry.getValue();
                        Object objectValue = nbt.getValue();
                        if (objectValue instanceof String value) {
                            stringMap.put(entry.getKey(), value);
                        }
                        else {
                            LOGGER.warn("Unexpected NBT value type " + objectValue.getClass().getTypeName() + "; " +
                                    "needs to be convertable to String, in window at ~" + center);
                        }
                    }

                    block = block.withProperties(stringMap);
                }

                repairBlocks.add(block);
            }
            catch (NBTException e) {
                LOGGER.warn("Failed to parse block SNBT for window at ~" + center, e);
            }
        }

        int repairBlockSize = repairBlocks.size();
        if (repairBlockSize != volume) {
            //try to fix the broken window data
            LOGGER.warn("Repair block list length (" + repairBlockSize + ") doesn't match window volume (" + volume +
                    "), for window at ~" + center);

            if (repairBlockSize < volume) {
                //fix too-short data by padding blocks
                //if empty, the padding block is DEFAULT_PADDING, if not empty, the padding block is the last block
                Block pad = repairBlockSize == 0 ? DEFAULT_PADDING : repairBlocks.get(repairBlockSize - 1);
                for (int i = repairBlockSize; i < volume; i++) {
                    repairBlocks.add(pad);
                }

                LOGGER.warn(
                        "Tried to fix window data by padding " + (volume - repairBlockSize) + " blocks of type " + pad +
                                " for window at ~" + center);
            }
            else {
                //fix too-long data by removing the extra entries
                repairBlocks.subList(volume, repairBlockSize).clear();
                repairBlocks.trimToSize();

                LOGGER.warn("Tried to fix window data by removing " + (repairBlockSize - volume) + " additional " +
                        "blocks in window at ~" + center);
            }
        }

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

    private Point indexToCoordinate(int index) {
        Bounds3I frameRegion = windowInfo.frameRegion();

        int x = index % frameRegion.lengthX();
        int y = (index / frameRegion.lengthX()) % frameRegion.lengthY();
        int z = (index / (frameRegion.lengthX() * frameRegion.lengthY())) % frameRegion.lengthZ();

        return worldMin.add(x, y, z);
    }
}
