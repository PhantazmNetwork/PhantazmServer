package com.github.phantazmnetwork.api.inventory;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Basic implementation of an {@link InventoryObject}.
 */
public class BasicInventoryObject implements InventoryObject {

    private ItemStack itemStack;

    /**
     * Creates a basic {@link InventoryObject}.
     * @param itemStack The initial {@link ItemStack} of the {@link InventoryObject}
     */
    public BasicInventoryObject(@NotNull ItemStack itemStack) {
        this.itemStack = Objects.requireNonNull(itemStack, "itemStack");
    }

    @Override
    public @NotNull ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public void setItemStack(@NotNull ItemStack itemStack) {
        Objects.requireNonNull(itemStack, "itemStack");
        this.itemStack = itemStack;
    }

}
