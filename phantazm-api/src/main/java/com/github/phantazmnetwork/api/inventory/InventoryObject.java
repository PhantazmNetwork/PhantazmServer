package com.github.phantazmnetwork.api.inventory;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an item in an {@link net.minestom.server.inventory.AbstractInventory}.
 */
public interface InventoryObject {

    /**
     * Updates the {@link ItemStack}'s representation in the associated {@link net.minestom.server.inventory.AbstractInventory}.
     * @param slot The slot in which the {@link ItemStack} should be updated in
     * @param hidden Whether the object will be considered hidden
     */
    void updateInInventory(int slot, boolean hidden);

    /**
     * Gets the currently associated {@link ItemStack}.
     * @return The currently associated {@link ItemStack}
     */
    @NotNull ItemStack getItemStack();

    /**
     * Sets the {@link ItemStack} used by this inventory object.
     * @param itemStack The {@link ItemStack} to use
     */
    void setItemStack(@NotNull ItemStack itemStack);

}
