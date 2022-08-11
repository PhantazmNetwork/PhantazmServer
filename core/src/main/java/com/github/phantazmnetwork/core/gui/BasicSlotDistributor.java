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

        int[] slots = new int[itemCount];

        return slots;
    }

    private static void fillRow(int[] slots, int slotStartIndex, int width, int padding, int rowStartIndex, int items) {
        //true if additional adjustments should be made to necessary slots in order to keep them visually centered
        boolean centerAdjust = (padding > 0 || items < width) && ((items % 2 == 0) == (width % 2 != 0));

        //the actual length of the row, including padding
        int rowLength = computeLength(items, padding);

        //the number of unused spaces that aren't included in padding
        int leftover = width - rowLength;

        //the initial offset to use in order to try and keep the row centered
        int baseOffset = leftover / 2;

        int slot = rowStartIndex + baseOffset;
        int center = (items / 2) - 1;
        for (int j = 0; j < items; j++) {
            slots[slotStartIndex++] = (centerAdjust && ((padding > 0) == (j <= center))) ? slot + 1 : slot;
            slot += padding + 1;
        }
    }

    private static int computeLength(int items, int padding) {
        return items + (padding * Math.max(0, items - 1));
    }
}
