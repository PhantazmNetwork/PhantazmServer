package com.github.phantazmnetwork.core.gui;

import com.github.phantazmnetwork.commons.MathUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Basic implementation of {@link SlotDistributor}. Tries to ensure items are centered, with horizontal and vertical
 * symmetry.
 */
public class BasicSlotDistributor implements SlotDistributor {
    private final int padding;

    /**
     * Creates a new instance of this class.
     *
     * @param padding the number of slots that the algorithm should try to maintain between items
     * @throws IllegalArgumentException if {@code padding < 0}
     */
    public BasicSlotDistributor(int padding) {
        if (padding < 0) {
            throw new IllegalArgumentException("padding < 0");
        }

        this.padding = padding;
    }

    @Override
    public int @NotNull [] distribute(int width, int height, int itemCount) {
        int size = width * height;
        if (itemCount > size) {
            throw new IllegalArgumentException(
                    "Distributed item count " + itemCount + " cannot be greater than size " + size);
        }
        
        int rows;
        int rowFactor;
        int totalHeight;
        int actualPadding = padding;
        do {
            rows = computeRows(width, actualPadding, itemCount);
            rowFactor = Math.max(0, rows - 1);
            totalHeight = rows + (actualPadding * rowFactor);
        } while (totalHeight > height && --actualPadding > -1);

        int maxItems = MathUtils.ceilDiv(width, actualPadding + 1);
        int leftover = itemCount % maxItems;
        if (leftover == 0) {
            leftover = maxItems;
        }

        boolean rowAdjust = canAdjust(totalHeight, height);
        int actualHeight = computeSize(rows, actualPadding);
        int rowSlot = (height - actualHeight) / 2;
        int rowCenter = (rows / 2) - 1;

        int[] slots = new int[itemCount];
        for (int i = 0, slotIndex = 0, rowItems; i < rows; i++, slotIndex += rowItems, rowSlot += actualPadding + 1) {
            int rowStartIndex = applyOffset(rowAdjust, actualPadding, rowCenter, i, rowSlot) * width;
            rowItems = i < rows - 1 ? maxItems : leftover;
            fillRow(slots, slotIndex, width, actualPadding, rowStartIndex, rowItems);
        }

        return slots;
    }

    private static void fillRow(int[] slots, int slotStartIndex, int width, int padding, int rowStartIndex, int items) {
        //the actual length of the row, including padding
        int actualWidth = computeSize(items, padding);

        //true if additional adjustments should be made to necessary slots in order to keep them visually centered
        boolean slotAdjust = canAdjust(actualWidth, width);

        //the initial offset to use in order to try and keep the row centered
        int baseOffset = (width - actualWidth) / 2;

        int slot = rowStartIndex + baseOffset;
        int center = (items / 2) - 1;
        for (int j = 0; j < items; j++, slot += padding + 1) {
            slots[slotStartIndex++] = applyOffset(slotAdjust, padding, center, j, slot);
        }
    }

    private static int computeSize(int items, int padding) {
        //compute the length of a single row of slots of "items" items separated by "padding" empty slots
        return items + (padding * (items - 1));
    }

    private static int computeRows(int width, int padding, int items) {
        int itemsPerRow = MathUtils.ceilDiv(width, padding + 1);
        return MathUtils.ceilDiv(items, itemsPerRow);
    }

    private static int applyOffset(boolean adjust, int padding, int centerIndex, int index, int slot) {
        return (adjust && ((padding > 0) == (index <= centerIndex))) ? slot + 1 : slot;
    }

    private static boolean canAdjust(int actualSize, int capacity) {
        return (actualSize % 2 == 0) != (capacity % 2 == 0);
    }
}
