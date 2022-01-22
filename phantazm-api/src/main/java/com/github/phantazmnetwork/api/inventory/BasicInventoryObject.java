package com.github.phantazmnetwork.api.inventory;

import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Basic implementation of an {@link InventoryObject}.
 */
public class BasicInventoryObject implements InventoryObject {

    private final Supplier<Optional<AbstractInventory>> inventorySupplier;

    private ItemStack itemStack;

    /**
     * Creates a basic {@link InventoryObject}.
     * @param inventorySupplier A {@link Supplier} which will supply the current representation of an {@link AbstractInventory}
     * @param itemStack The initial {@link ItemStack} of the {@link InventoryObject}
     */
    public BasicInventoryObject(@NotNull Supplier<Optional<AbstractInventory>> inventorySupplier,
                                @NotNull ItemStack itemStack) {
        this.inventorySupplier = Objects.requireNonNull(inventorySupplier, "inventorySupplier");
        this.itemStack = Objects.requireNonNull(itemStack, "itemStack");
    }

    @Override
    public void updateInInventory(int slot, boolean visible) {
        ItemStack stack = visible ? itemStack : ItemStack.AIR;
        inventorySupplier.get().ifPresent(inventory -> inventory.setItemStack(slot, stack));
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
