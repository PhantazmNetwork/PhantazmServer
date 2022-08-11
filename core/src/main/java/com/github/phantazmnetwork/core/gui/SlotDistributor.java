package com.github.phantazmnetwork.core.gui;

import org.jetbrains.annotations.NotNull;

/**
 * An object that can determine the arrangement of items in a rectangular inventory space.
 */
@FunctionalInterface
public interface SlotDistributor {
    /**
     * Distributes the given number of items throughout an inventory of the given size. The returned array will contain
     * slot numbers representing the suggested position of items within the inventory grid.
     *
     * @param width     the inventory width
     * @param height    the inventory height
     * @param itemCount the number of items to distribute
     * @return an array, of length {@code itemCount}, containing the suggested item positions
     * @throws IllegalArgumentException if {@code itemCount > width * height}
     */
    int @NotNull [] distribute(int width, int height, int itemCount);
}
