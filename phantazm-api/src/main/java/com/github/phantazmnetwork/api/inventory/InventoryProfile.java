package com.github.phantazmnetwork.api.inventory;

import org.jetbrains.annotations.NotNull;

/**
 * A profile of an inventory.
 */
public interface InventoryProfile {

    /**
     * Checks whether the profile has an {@link InventoryObject} in a slot
     * @param slot The slot to check
     * @return Whether the profile has an {@link InventoryObject}
     */
    default boolean hasInventoryObject(int slot) {
        return getInventoryObject(slot) != null;
    }

    /**
     * Gets the {@link InventoryObject} within a certain slot.
     * @param slot The slot to get the {@link InventoryObject} from
     * @return The {@link InventoryObject}, or null if none was in the slot
     */
    InventoryObject getInventoryObject(int slot);

    /**
     * Sets the {@link InventoryObject} within a certain slot.
     * @param slot The slot to put the {@link InventoryObject} into
     * @param object The {@link InventoryObject} to put in, or null to remove the inventory object
     * @throws IllegalStateException If an {@link InventoryObject} is in the current slot
     */
    void setInventoryObject(int slot, @NotNull InventoryObject object);

    /**
     * Removes the {@link InventoryObject} within a certain slot.
     * @param slot The slot to remove an {@link InventoryObject} from
     */
    void removeInventoryObject(int slot);

    /**
     * Gets the number of slots this profile holds.
     * @return The number of slots this profile holds
     */
    int getSlotCount();

    /**
     * Checks whether the profile is currently visible.
     * @return Whether the profile is currently visible
     */
    boolean isVisible();

    /**
     * Sets the profile's visibility.
     * @param visible The profile's visibility
     */
    void setVisible(boolean visible);

}
