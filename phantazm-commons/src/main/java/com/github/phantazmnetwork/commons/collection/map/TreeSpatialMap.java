package com.github.phantazmnetwork.commons.collection.map;

import com.github.phantazmnetwork.commons.collection.list.GapList;
import com.github.phantazmnetwork.commons.collection.list.UnsafeGapList;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class TreeSpatialMap<TValue> implements SpatialMap<TValue> {
    private static class Block {
        private int startHi;
        private long startLo;
        private int length;
        private int compressedIndex;

        private Block(int startHi, long startLo, int length, int compressedIndex) {
            this.startHi = startHi;
            this.startLo = startLo;
            this.length = length;
        }

        private boolean endEquals(int hi, long lo) {
            long endLo = startLo + length;
            int endHi = computeHi(startHi, startLo, 0, length, endLo);
            return endLo == lo && endHi == hi;
        }
    }

    private static final long INDEX_MASK = 0x0000_0000_FFFF_FFFFL;
    private static final long OFFSET_MASK = 0xFFFF_FFFF_0000_0000L;
    private static final long OFFSET_NEGATIVE_1 = 0xFFFF_FFFF_0000_0000L;

    private final GapList<TValue> list;
    private final GapList<Block> blocks;

    public TreeSpatialMap() {
        this.list = new UnsafeGapList<>();
        this.blocks = new UnsafeGapList<>();
    }

    private static long computeLow(int y, int z) {
        return ((long)y << 32) | z;
    }

    //comparison method for 96-bit unsigned keys
    private static int compare(int firstHi, long firstLo, int secondHi, long secondLo) {
        int hiCompare = Integer.compareUnsigned(firstHi, secondHi);
        if(hiCompare == 0) {
            return Long.compareUnsigned(firstLo, secondLo);
        }

        return hiCompare;
    }

    private static int computeHi(int hiFirst, long loFirst, int hiSecond, long loSecond, long loSum) {
        return (int) (hiFirst + hiSecond + ((loFirst ^ loSum) & (loSecond ^ loSum)));
    }

    private static int unpackIndex(long data) {
        return (int) (data & INDEX_MASK);
    }

    private static int unpackOffset(long data) {
        return (int) ((data & OFFSET_MASK) >>> 32);
    }

    //specialized binary search algorithm to find blocks keyed by 96-bit integers
    private static long nearest(GapList<Block> blocks, int hi, long lo) {
        int lowIndex = 0;
        int highIndex = blocks.size() - 1;

        while (lowIndex <= highIndex) {
            int mid = (lowIndex + highIndex) >>> 1;
            Block midVal = blocks.getUnsafe(mid);
            int comparison = compare(midVal.startHi, midVal.startLo, hi, lo);

            //note: test code should cover cases where midVal.startLo will be == -1 as well as max/min long values
            if (comparison < 0) { //midVal < target
                //we may have found our block, but only if we're in range
                //do some arithmetic on our conceptual 96-bit unsigned integers to find out
                //add midVal.start and midVal.length
                long endLo = midVal.startLo + midVal.length;

                //handles integer overflow, treats values as unsigned
                int endHi = computeHi(midVal.startHi, midVal.startLo, 0, midVal.length, endLo);

                //we found a block, we can stop searching
                //note that this code also returns if we're directly after a block, with no gap
                if(compare(hi, lo, endHi, endLo) <= 0) {
                    return ((lo - midVal.startLo) << Integer.SIZE) | mid;
                }

                lowIndex = mid + 1;
            }
            else if (comparison > 0) { //midVal > target
                //check if we're right before a block as well
                //blocks will never be directly adjacent, there will always be a gap of at least 1 element
                //add midVal.start and -lo
                int offsetHi = midVal.startHi;
                long offsetLo = midVal.startLo - 1;
                if(offsetLo == 0) {
                    offsetHi--;
                }

                //we're directly below a block
                if(hi == offsetHi && lo == offsetLo) {
                    return OFFSET_NEGATIVE_1 | mid;
                }

                highIndex = mid - 1;
            }
            else {
                return mid;
            }
        }

        return -(lowIndex + 1);  //block not found
    }

    private static void incrementIndices(GapList<Block> blocks, int startingAt) {
        for(int i = startingAt; i < blocks.size(); i++) {
            blocks.getUnsafe(i).compressedIndex++;
        }
    }

    @Override
    public TValue get(int x, int y, int z) {
        long lo = computeLow(y, z);
        long data = nearest(blocks, x, lo);
        int blockIndex = unpackIndex(data);
        if(blockIndex > 0) {
            Block block = blocks.getUnsafe(blockIndex);
            int offset = unpackOffset(data);
            if(offset >= 0 && offset < block.length) {
                return list.getUnsafe(block.compressedIndex + offset);
            }
        }

        return null;
    }

    @Override
    public void put(int x, int y, int z, TValue value) {
        long lo = computeLow(y, z);

        long data = nearest(blocks, x, lo);
        int blockIndex = unpackIndex(data);

        if(blockIndex < 0) { //we have to create a block rather than extending an existing block
            blockIndex = (-blockIndex) - 1;
            int compressedIndex = blocks.size() == 0 ? 0 : blocks.getUnsafe(blockIndex).compressedIndex;

            list.addUnsafe(compressedIndex, value);
            blocks.addUnsafe(blockIndex, new Block(x, lo, 1, compressedIndex));
        }
        else { //we found a block
            int offset = unpackOffset(data);
            Block block = blocks.getUnsafe(blockIndex);

            //check if we need to merge blocks now
            if(offset == -1) { //directly before block
                list.addUnsafe(block.compressedIndex, value);
                block.length++;

                long newStartLo = block.startLo - 1;
                int newStartHi = block.startHi;
                if(newStartLo == 0) {
                    newStartHi--;
                }

                block.startLo = newStartLo;
                block.startHi = newStartHi;

                int previousBlockIndex = blockIndex - 1;
                if(previousBlockIndex > 0) {
                    Block previousBlock = blocks.getUnsafe(previousBlockIndex);

                    if(previousBlock.endEquals(newStartHi, newStartLo)) {
                        block.startHi = previousBlock.startHi;
                        block.startLo = previousBlock.startLo;

                        block.length = previousBlock.length + 1;
                        blocks.removeUnsafe(previousBlockIndex);
                    }
                }
            }
            else { //some point inside or directly after block
                list.addUnsafe(block.compressedIndex + offset, value);
                block.length++;

                int nextBlockIndex = blockIndex + 1;
                if(nextBlockIndex < blocks.size()) {
                    Block nextBlock = blocks.getUnsafe(nextBlockIndex);

                    if(block.endEquals(nextBlock.startHi, nextBlock.startLo)) {
                        block.length += nextBlock.length;
                        blocks.removeUnsafe(nextBlockIndex);
                    }
                }
            }
        }

        incrementIndices(blocks, blockIndex + 1);
    }

    @Override
    public boolean containsKey(int x, int y, int z) {
        return false;
    }

    @Override
    public void remove(int x, int y, int z) {

    }

    @Override
    public int size() {
        return 0;
    }

    @NotNull
    @Override
    public Iterator<TValue> iterator() {
        return list.listIterator();
    }
}
