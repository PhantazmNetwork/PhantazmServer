package com.github.phantazmnetwork.core.gui;

public class BasicSlotDistributor implements SlotDistributor {
    private final int padding;

    public BasicSlotDistributor(int padding) {
        if (padding < 0) {
            throw new IllegalArgumentException("padding cannot be < 0");
        }

        this.padding = padding;
    }

    @Override
    public int[] distribute(int width, int height, int itemCount) {
        int size = width * height;
        if (itemCount > size) {
            throw new IllegalArgumentException(
                    "Distributed item count " + itemCount + " cannot be greater than size " + size);
        }

        int[] slots = new int[itemCount];

        int totalWidth;
        int totalHeight;
        int paddingWidth = this.padding;
        int paddingHeight = this.padding;

        do {
            totalWidth = itemCount + (paddingWidth * Math.max(0, itemCount - 1));
        } while (totalWidth > width && paddingWidth-- > -1);

        int rows = (int)Math.ceil((double)totalWidth / (double)width);
        do {
            totalHeight = rows + (paddingHeight * Math.max(0, rows - 1));
        } while (totalHeight > height && paddingHeight-- > -1);


        return slots;
    }
}
