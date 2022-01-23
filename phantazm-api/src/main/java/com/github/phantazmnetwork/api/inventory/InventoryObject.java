package com.github.phantazmnetwork.api.inventory;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an item in an {@link net.minestom.server.inventory.AbstractInventory}.
 */
public interface InventoryObject {

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
