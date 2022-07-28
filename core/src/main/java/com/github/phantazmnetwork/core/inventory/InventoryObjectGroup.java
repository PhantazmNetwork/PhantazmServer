package com.github.phantazmnetwork.core.inventory;

import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

/**
 * Represents a group of {@link InventoryObject}s.
 */
public interface InventoryObjectGroup {

    /**
     * Adds a slot to the group.
     *
     * @param slot The slot to add
     * @throws IllegalArgumentException If the group already contains the slot
     */
    void addSlot(int slot);

    /**
     * Removes a slot from the group.
     *
     * @param slot The slot to remove
     * @throws IllegalArgumentException If the group does not contain the slot
     */
    void removeSlot(int slot);

    /**
     * Gets an unmodifiable view of the group's slots.
     *
     * @return An unmodifiable view of the group's slots
     */
    @NotNull @UnmodifiableView IntSet getSlots();

    /**
     * Checks whether the group within a profile is considered full.
     * This means that no {@link InventoryObject}s may be pushed to the group.
     *
     * @return Whether the group is considered full
     */
    boolean isFull();

    /**
     * Checks whether the group within a profile is considered empty.
     * This means that no {@link InventoryObject}s may be popped from the group.
     *
     * @return Whether the group is considered empty
     */
    boolean isEmpty();

    /**
     * Computes the next slot that an {@link InventoryObject} should be placed in.
     *
     * @param toPush The {@link InventoryObject} to push
     * @throws IllegalArgumentException If the group's representation is full (check this against {@link #isFull()})
     */
    void pushInventoryObject(@NotNull InventoryObject toPush);

    /**
     * Computes the next slot that an {@link InventoryObject} should be popped from.
     *
     * @return The {@link InventoryObject} that was popped
     * @throws IllegalArgumentException If the group's representation is empty (check this against {@link #isEmpty()})
     */
    @NotNull InventoryObject popInventoryObject();

}
