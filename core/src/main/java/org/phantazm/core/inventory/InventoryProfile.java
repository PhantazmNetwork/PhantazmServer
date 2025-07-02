package org.phantazm.core.inventory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

public interface InventoryProfile {
    @Nullable InventoryObject getInventoryObject(int slot);

    @Nullable InventoryObject setInventoryObject(int slot, @Nullable InventoryObject object);

    boolean compareAndSet(int slot, @Nullable InventoryObject witness, @Nullable InventoryObject insert);

    @Nullable InventoryObject getAndUpdate(int slot, UnaryOperator<InventoryObject> operator);

    int getSlotCount();

    @NotNull Iterable<? extends InventoryObject> objects();
}
