package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.commons.vector.Region3I;
import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.zombies.map.WindowInfo;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.*;

public class Window extends MapObject<WindowInfo> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Window.class);
    private static final Block DEFAULT_PADDING = Block.OAK_SLAB;

    private final Vec3I worldMin;
    private final Vec3D center;
    private final int volume;
    private final ArrayList<Block> repairBlocks;
    private final Block brokenBlock;

    private int index;

    public Window(@NotNull WindowInfo data, @NotNull Vec3I origin, @NotNull Block brokenBlock) {
        super(data, origin);
        Region3I frame = data.frameRegion();
        Vec3I min = frame.getOrigin();
        Vec3I lengths = frame.getLengths();

        worldMin = Vec3I.of(origin.getX() + min.getX(), origin.getY() + min.getY(), origin.getZ() + min.getZ());
        center = Vec3D.of(worldMin.getX() + ((double)lengths.getX() / 2D), worldMin.getY() +
                ((double)lengths.getY() / 2D), worldMin.getZ() + ((double)lengths.getZ() / 2D));
        volume = frame.volume();

        List<String> repairBlockSnbts = data.repairBlocks();
        repairBlocks = new ArrayList<>(repairBlockSnbts.size());
        this.brokenBlock = Objects.requireNonNull(brokenBlock, "brokenBlock");
        for(String blockString : repairBlockSnbts) {
            try {
                NBTCompound compound = (NBTCompound) new SNBTParser(new StringReader(blockString)).parse();
                String id = compound.getString("Name");
                if(id == null) {
                    LOGGER.warn("Malformed block SNBT " + compound + ", no Name tag found in block data for window " +
                            "at ~" + center);
                    return;
                }

                Block block = Block.fromNamespaceId(id);
                if(block == null) {
                    LOGGER.warn("Found a block with unknown id " + id + " in window at ~" + center);
                    return;
                }

                NBTCompound properties = compound.getCompound("Properties");

                if(properties != null) {
                    Map<String, NBT> propertiesMap = properties.asMapView();
                    Map<String, String> stringMap = new HashMap<>(propertiesMap.size());

                    for(Map.Entry<String, NBT> entry : properties.getEntries()) {
                        NBT nbt = entry.getValue();
                        Object objectValue = nbt.getValue();
                        if(objectValue instanceof String value) {
                            stringMap.put(entry.getKey(), value);
                        }
                        else {
                            LOGGER.warn("Unexpected NBT value type " + objectValue.getClass().getTypeName() +
                                    "; needs to be convertable to String, in window at ~" + center);
                        }
                    }

                    block = block.withProperties(stringMap);
                }

                repairBlocks.add(block);
            } catch (NBTException e) {
                LOGGER.warn("Failed to parse block SNBT for window at ~" + center, e);
            }
        }

        int repairBlockSize = repairBlocks.size();
        if(repairBlockSize != volume) {
            //try to fix the broken window data
            LOGGER.warn("Repair block list length (" + repairBlockSize + ") doesn't match window volume (" + volume +
                    "), for window at ~" + center);

            if(repairBlockSize < volume) {
                //fix too-short data by padding blocks
                //if empty, the padding block is DEFAULT_PADDING, if not empty, the padding block is the last block
                Block pad = repairBlockSize == 0 ? DEFAULT_PADDING : repairBlocks.get(repairBlockSize - 1);
                for(int i = repairBlockSize; i < volume; i++) {
                    repairBlocks.add(pad);
                }

                LOGGER.warn("Tried to fix window data by padding " + (volume - repairBlockSize) + " blocks of type " +
                        pad + " for window at ~" + center);
            }
            else {
                //fix too-long data by removing the extra entries
                repairBlocks.subList(volume, repairBlockSize).clear();
                repairBlocks.trimToSize();

                LOGGER.warn("Tried to fix window data by removing " + (repairBlockSize - volume) + " additional " +
                        "blocks in window at ~" + center);
            }
        }
    }

    /**
     * Updates the window index, playing the appropriate effects depending on if the index increased (repair) or
     * decreased (break).
     * @param instance the instance this window is in
     * @param newIndex the new break index
     * @throws IndexOutOfBoundsException if newIndex is &lt; 0 or &gt; volume
     */
    public void updateIndex(@NotNull Instance instance, int newIndex) {
        Objects.checkIndex(newIndex, volume + 1);
        if(newIndex == index) {
            return; //no change
        }

        if(newIndex < index) {
            //play the break sound
            instance.playSound(newIndex == 0 ? data.breakAllSound() : data.breakSound(), center.getX(), center.getY(),
                    center.getZ());

            for(int i = index - 1; i >= newIndex; i--) {
                Vec3I breakLocation = indexToCoordinate(i);
                instance.setBlock(breakLocation.getX(), breakLocation.getY(), breakLocation.getZ(), brokenBlock);
            }
        }
        else {
            //play the repair sound
            instance.playSound(newIndex == volume ? data.repairAllSound() : data.repairSound(), center.getX(),
                    center.getY(), center.getZ());

            for(int i = index; i <= newIndex; i++) {
                Vec3I repairLocation = indexToCoordinate(i);
                instance.setBlock(repairLocation.getX(), repairLocation.getY(), repairLocation.getZ(), repairBlocks
                        .get(i));
            }
        }

        index = newIndex;
    }

    /**
     * Checks if this window is fully repaired or not. Equivalent to {@code getRepairIndex() == getVolume()}.
     * @return true if this window has been fully repaired, false otherwise
     */
    public boolean isRepaired() {
        return index == volume;
    }

    /**
     * Returns the current "repair index". This is equal to 0 for a fully broken window and {@code getVolume()} for
     * a fully repaired window.
     * @return the current repair index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets the number of blocks in the window face.
     * @return the number of blocks in the window face
     */
    public int getVolume() {
        return volume;
    }

    /**
     * Gets the center of the window frame region in world coordinates. This is the point from which distance to the
     * window should be measured.
     * @return the center of the window
     */
    public @NotNull Vec3D getCenter() {
        return center;
    }

    private Vec3I indexToCoordinate(int index) {
        Vec3I lengths = data.frameRegion().getLengths();

        return Vec3I.ORIGIN;
    }
}
