package com.github.phantazmnetwork.core.inventory;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Basic implementation of an {@link InventoryProfile}.
 */
public class BasicInventoryProfile implements InventoryProfile {

    private final InventoryObject[] objects;

    /**
     * Creates a basic inventory profile.
     *
     * @param slotCount The number of slots held by the profile (indexed by 0)
     */
    public BasicInventoryProfile(int slotCount) {
        this.objects = new InventoryObject[slotCount];
    }

    @Override
    public boolean hasInventoryObject(int slot) {
        return objects[slot] != null;
    }

    @Override
    public @NotNull InventoryObject getInventoryObject(int slot) {
        if (!hasInventoryObject(slot)) {
            throw new IllegalArgumentException("No inventory object in slot");
        }

        return objects[slot];
    }

    @Override
    public void setInventoryObject(int slot, @NotNull InventoryObject object) {
        Objects.requireNonNull(object, "object");

        if (hasInventoryObject(slot)) {
            throw new IllegalArgumentException("Can't set inventory object in occupied slot");
        }

        objects[slot] = object;
    }

    @Override
    public void removeInventoryObject(int slot) {
        if (!hasInventoryObject(slot)) {
            throw new IllegalArgumentException("Can't remove inventory object from unoccupied slot");
        }

        objects[slot] = null;
    }

    @Override
    public int getSlotCount() {
        return objects.length;
    }

}
