package com.github.phantazmnetwork.api.inventory;

import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Abstract implementation of an {@link InventoryObjectGroup}.
 */
public abstract class InventoryObjectGroupAbstract implements InventoryObjectGroup {

    private final IntSet slots;

    /**
     * Creates an {@link InventoryObjectGroupAbstract}.
     * @param slots The slots to use for the group
     */
    public InventoryObjectGroupAbstract(@NotNull IntSet slots) {
        this.slots = Objects.requireNonNull(slots, "slots");
    }

    @Override
    public void addSlot(int slot) {
        if (!slots.add(slot)) {
            throw new IllegalArgumentException("Slot already added");
        }
    }

    @Override
    public void removeSlot(int slot) {
        if (!slots.remove(slot)) {
            throw new IllegalArgumentException("Slot not yet added");
        }
    }

    @Override
    public @NotNull IntSet getSlots() {
        return IntSets.unmodifiable(slots);
    }

    @Override
    public boolean isFull(@NotNull InventoryProfile profile) {
        for (int slot : slots) {
            if (profile.getInventoryObject(slot) == null) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isEmpty(@NotNull InventoryProfile profile) {
        for (int slot : slots) {
            if (profile.getInventoryObject(slot) == null) {
                return true;
            }
        }

        return false;
    }

}
