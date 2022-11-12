package com.github.phantazmnetwork.core.inventory;

import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class BasicInventoryObjectGroup extends InventoryObjectGroupAbstract {

    public BasicInventoryObjectGroup(@NotNull InventoryProfile profile, @NotNull IntSet slots,
            @NotNull Function<? super IntSet, ? extends IntSet> unmodifiableMapper) {
        super(profile, slots, unmodifiableMapper);
    }

    public BasicInventoryObjectGroup(@NotNull InventoryProfile profile, @NotNull IntSet slots) {
        super(profile, slots);
    }

    @Override
    public void pushInventoryObject(@NotNull InventoryObject toPush) {
        for (int slot : getSlots()) {
            if (!getProfile().hasInventoryObject(slot)) {
                getProfile().setInventoryObject(slot, toPush);
                return;
            }
        }

        throw new IllegalStateException("All slots are full");
    }

    @Override
    public @NotNull InventoryObject popInventoryObject() {
        for (int i = getSlots().size(); i >= 0; i--) {
            if (getProfile().hasInventoryObject(i)) {
                InventoryObject object = getProfile().getInventoryObject(i);
                getProfile().removeInventoryObject(i);
                return object;
            }
        }

        throw new IllegalStateException("All slots are empty");
    }
}
