package com.github.phantazmnetwork.core.gui;

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

        int[] slots = new int[itemCount];
        int totalWidth = itemCount + (padding * Math.max(0, itemCount - 1));

        int rows = (int)Math.ceil((double)totalWidth / (double)width);
        int rowFactor = Math.max(0, rows - 1);
        int totalHeight = rows + (padding * rowFactor);

        int actualPadding = padding;
        if (totalHeight > height) {
            //calculate the smallest padding value that will allow everything to fit
            actualPadding = (totalHeight - rows) / rowFactor;
        }

        
        return slots;
    }
}
