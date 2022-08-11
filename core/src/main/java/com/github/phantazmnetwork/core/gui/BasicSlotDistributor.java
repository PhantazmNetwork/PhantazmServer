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

        int totalWidth = computeSize(itemCount, padding);
        int rows = MathUtils.ceilDiv(totalWidth, width);
        int rowFactor = Math.max(0, rows - 1);
        int totalHeight = rows + (padding * rowFactor);

        int actualPadding = padding;
        if (totalHeight > height) {
            //calculate the smallest padding value that will allow everything to fit
            actualPadding = (totalHeight - rows) / rowFactor;
        }

        int rowItems = MathUtils.ceilDiv(width, actualPadding + 1);

        boolean rowAdjust = canAdjust(actualPadding, rows, height);
        int actualHeight = computeSize(rows, actualPadding);
        int rowSlot = (height - actualHeight) / 2;
        int rowCenter = (rows / 2) - 1;

        int[] slots = new int[itemCount];
        for (int i = 0, slotIndex = 0, itemsThisRow; i < rows;
                i++, slotIndex += itemsThisRow, rowSlot += actualPadding + 1) {
            int rowStartIndex = applyOffset(rowAdjust, actualPadding, rowCenter, i, rowSlot) * width;
            itemsThisRow = i < rows - 1 ? rowItems : (itemCount == rowItems ? rowItems : itemCount % rowItems);
            fillRow(slots, slotIndex, width, actualPadding, rowStartIndex, itemsThisRow);
        }

        return slots;
    }

    private static void fillRow(int[] slots, int slotStartIndex, int width, int padding, int rowStartIndex, int items) {
        //true if additional adjustments should be made to necessary slots in order to keep them visually centered
        boolean slotAdjust = canAdjust(padding, items, width);

        //the actual length of the row, including padding
        int actualWidth = computeSize(items, padding);

        //the initial offset to use in order to try and keep the row centered
        int baseOffset = (width - actualWidth) / 2;

        int slot = rowStartIndex + baseOffset;
        int center = (items / 2) - 1;
        for (int j = 0; j < items; j++, slot += padding + 1) {
            slots[slotStartIndex++] = applyOffset(slotAdjust, padding, center, j, slot);
        }
    }

    private static int applyOffset(boolean adjust, int padding, int centerIndex, int index, int slot) {
        return (adjust && ((padding > 0) == (index <= centerIndex))) ? slot + 1 : slot;
    }

    private static boolean canAdjust(int padding, int items, int size) {
        return (padding > 0 || items < size) && ((items % 2 == 0) == (size % 2 != 0));
    }

    private static int computeSize(int items, int padding) {
        return items + (padding * Math.max(0, items - 1));
    }
}
