package org.phantazm.core.inventory;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class StaticInventoryObject implements InventoryObject {
    private final ItemStack itemStack;

    public StaticInventoryObject(@NotNull ItemStack itemStack) {
        this.itemStack = Objects.requireNonNull(itemStack, "itemStack");
    }

    @Override
    public void tick(long time) {

    }

    @Override
    public @NotNull ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public boolean shouldRedraw() {
        return true;
    }
}
