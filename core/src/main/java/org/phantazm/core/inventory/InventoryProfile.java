package org.phantazm.core.inventory;

import org.jetbrains.annotations.NotNull;

/**
 * A profile of an inventory.
 */
public interface InventoryProfile {

    /**
     * Checks whether the profile has an {@link InventoryObject} in a slot
     *
     * @param slot The slot to check
     * @return Whether the profile has an {@link InventoryObject}
     */
    boolean hasInventoryObject(int slot);

    /**
     * Gets the {@link InventoryObject} within a certain slot. This should be checked first with {@link #hasInventoryObject(int)}.
     *
     * @param slot The slot to get the {@link InventoryObject} from
     * @return The {@link InventoryObject}
     * @throws IllegalArgumentException If no {@link InventoryObject} exists in the slot
     */
    @NotNull InventoryObject getInventoryObject(int slot);

    /**
     * Sets the {@link InventoryObject} within a certain slot.
     *
     * @param slot   The slot to put the {@link InventoryObject} into
     * @param object The {@link InventoryObject} to put in, or null to remove the inventory object
     * @throws IllegalStateException If an {@link InventoryObject} is in the current slot
     */
    void setInventoryObject(int slot, @NotNull InventoryObject object);

    /**
     * Removes the {@link InventoryObject} within a certain slot.
     *
     * @param slot The slot to remove an {@link InventoryObject} from
     */
    void removeInventoryObject(int slot);

    /**
     * Gets the number of slots this profile holds.
     *
     * @return The number of slots this profile holds
     */
    int getSlotCount();
}
