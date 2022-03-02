package com.github.phantazmnetwork.commons.collection.map;

import com.github.phantazmnetwork.commons.collection.list.GapList;
import com.github.phantazmnetwork.commons.collection.list.UnsafeGapList;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

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

    private static int computeHi(int hiFirst, long loFirst, int hiSecond, long loSecond) {
        long sum = loFirst + loSecond;
        return (int) (hiFirst + hiSecond + ((loFirst ^ sum) & (loSecond ^ sum)));
    }

    private static int unpackIndex(long data) {
        return (int) (data & INDEX_MASK);
    }

    private static int unpackOffset(long data) {
        return (int) ((data & OFFSET_MASK) >>> 32);
    }

    //modified binary search algorithm to search through blocks keyed by 96-bit integers
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
                int endHi = computeHi(midVal.startHi, midVal.startLo, 0, midVal.length);

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
                long offsetLo = midVal.startLo - lo;
                int offsetHi = computeHi(midVal.startHi, midVal.startLo, 0, -lo);

                //we're directly below a block
                if(offsetHi == midVal.startHi && offsetLo == 1) {
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
        return null;
    }

    @Override
    public void put(int x, int y, int z, TValue value) {
        long lo = computeLow(y, z);

        long data = nearest(blocks, x, lo);
        int blockIndex = unpackIndex(data);

        if(blockIndex < 0) { //we have to create a block rather than extending an existing block
            //when nearestIndex is negative, the entire result is interpreted as the index of the nearest block; there
            //is no additional data to unpack

            blockIndex = (-blockIndex) + 1;
            int compressedIndex = blocks.size() == 0 ? 0 : blocks.getUnsafe(blockIndex).compressedIndex;

            list.addUnsafe(compressedIndex, value);
            blocks.addUnsafe(blockIndex, new Block(x, lo, 1, compressedIndex));

            incrementIndices(blocks, blockIndex + 1);
        }
        else { //we found a block
            int offset = unpackOffset(data);
            Block block = blocks.getUnsafe(blockIndex);
            if(offset == -1) { //directly-before case

            }
            else { //in the middle of the block
                list.addUnsafe(block.compressedIndex + offset, value);
                incrementIndices(blocks, blockIndex + 1);
                block.length++;

                //check if we need to merge blocks now
                int nextBlockIndex = blockIndex + 1;
                if(nextBlockIndex < blocks.size()) {
                    Block nextBlock = blocks.getUnsafe(nextBlockIndex);

                }
            }
        }
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
        return null;
    }
}
