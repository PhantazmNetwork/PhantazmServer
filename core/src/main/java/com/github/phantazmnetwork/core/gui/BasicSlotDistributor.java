package com.github.phantazmnetwork.core.gui;

import com.github.phantazmnetwork.commons.MathUtils;

public class BasicSlotDistributor implements SlotDistributor {
    private final int padding;

    public BasicSlotDistributor(int padding) {
        this.padding = padding;
    }

    @Override
    public int[] distribute(int width, int height, int itemCount) {
        int size = width * height;
        if (itemCount > size) {
            throw new IllegalArgumentException(
                    "Distributed item count " + itemCount + " cannot be greater than size " + size);
        }

        int totalWidth = computeLength(itemCount, padding);
        int rows = MathUtils.ceilDiv(totalWidth, width);
        int rowFactor = Math.max(0, rows - 1);
        int totalHeight = rows + (padding * rowFactor);

        int actualPadding = padding;
        if (totalHeight > height) {
            //calculate the smallest padding value that will allow everything to fit
            actualPadding = (totalHeight - rows) / rowFactor;
        }

        boolean adjust = canAdjust(actualPadding, rows, height);
        int columnLength = computeLength(rows, actualPadding);
        int leftover = height - columnLength;
        int baseOffset = leftover / 2;

        int[] slots = new int[itemCount];
        int center = (rows / 2) - 1;

        int maxRowItems = MathUtils.ceilDiv(width, actualPadding + 1);
        int slotIndex = 0;
        for (int i = 0; i < rows; i++) {
            int rowStartIndex = computeSlot(adjust, actualPadding, center, i, baseOffset) * width;
            int itemsThisRow = i < rows - 1 ? maxRowItems : itemCount % maxRowItems;
            fillRow(slots, slotIndex, width, actualPadding, rowStartIndex, itemsThisRow);
        }

        return slots;
    }

    private static void fillRow(int[] slots, int slotStartIndex, int width, int padding, int rowStartIndex, int items) {
        //true if additional adjustments should be made to necessary slots in order to keep them visually centered
        boolean adjust = canAdjust(padding, items, width);

        //the actual length of the row, including padding
        int rowLength = computeLength(items, padding);

        //the number of unused spaces that aren't included in padding
        int leftover = width - rowLength;

        //the initial offset to use in order to try and keep the row centered
        int baseOffset = leftover / 2;

        int slot = rowStartIndex + baseOffset;
        int center = (items / 2) - 1;
        for (int j = 0; j < items; j++) {
            slots[slotStartIndex++] = computeSlot(adjust, padding, center, j, slot);
            slot += padding + 1;
        }
    }

    private static int computeSlot(boolean adjust, int padding, int center, int index, int slot) {
        return (adjust && ((padding > 0) == (index <= center))) ? slot + 1 : slot;
    }

    private static boolean canAdjust(int padding, int items, int size) {
        return (padding > 0 || items < size) && ((items % 2 == 0) == (size % 2 != 0));
    }

    private static int computeLength(int items, int padding) {
        return items + (padding * Math.max(0, items - 1));
    }
}
