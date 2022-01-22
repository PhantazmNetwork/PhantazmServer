package com.github.phantazmnetwork.api.inventory;

import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

/**
 * Represents a group of {@link InventoryObject}s.
 */
public interface InventoryObjectGroup {

    /**
     * Adds a slot to the group.
     * @param slot The slot to add
     * @throws IllegalArgumentException If the group already contains the slot
     */
    void addSlot(int slot);

    /**
     * Removes a slot from the group.
     * @param slot The slot to remove
     * @throws IllegalArgumentException If the group does not contain the slot
     */
    void removeSlot(int slot);

    /**
     * Gets an unmodifiable view of the group's slots.
     * @return An unmodifiable view of the group's slots
     */
    @NotNull @UnmodifiableView IntSet getSlots();

    /**
     * Checks whether the group within a profile is considered full.
     * This means that no {@link InventoryObject}s may be pushed to the group.
     * @param profile The {@link InventoryProfile} to check against
     * @return Whether the group is considered full
     */
    boolean isFull(@NotNull InventoryProfile profile);

    /**
     * Checks whether the group within a profile is considered empty.
     * This means that no {@link InventoryObject}s may be popped from the group.
     * @param profile The {@link InventoryProfile} to check against
     * @return Whether the group is considered empty
     */
    boolean isEmpty(@NotNull InventoryProfile profile);

    /**
     * Computes the next slot that an {@link InventoryObject} should be placed in.
     * @param profile The profile to compute the slot with
     * @return The slot to push the next {@link InventoryObject} to
     * @throws IllegalArgumentException If the group's representation in the profile is full (check this against {@link #isFull(InventoryProfile)})
     */
    int computeNextPushSlot(@NotNull InventoryProfile profile);

    /**
     * Computes the next slot that an {@link InventoryObject} should be popped from.
     * @param profile The profile to compute the slot with
     * @return The slot to pop the next {@link InventoryObject} from
     * @throws IllegalArgumentException If the group's representation in the profile is empty (check this against {@link #isEmpty(InventoryProfile)})
     */
    int computeNextPopSlot(@NotNull InventoryProfile profile);

}
