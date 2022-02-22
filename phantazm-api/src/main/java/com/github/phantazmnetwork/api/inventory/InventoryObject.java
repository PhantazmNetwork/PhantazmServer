package com.github.phantazmnetwork.api.inventory;

import com.github.phantazmnetwork.api.util.Tickable;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an item in an {@link net.minestom.server.inventory.AbstractInventory}.
 */
public interface InventoryObject extends Tickable {

    /**
     * Gets the currently associated {@link ItemStack}.
     * @return The currently associated {@link ItemStack}
     */
    @NotNull ItemStack getItemStack();

    /**
     * Checks whether this inventory object should be redrawn.
     * @return Whether this inventory object should be redrawn
     */
    boolean shouldRedraw();

}
