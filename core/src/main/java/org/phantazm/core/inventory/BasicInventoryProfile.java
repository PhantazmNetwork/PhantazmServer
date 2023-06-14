package org.phantazm.core.inventory;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
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
    public @NotNull InventoryObject setInventoryObject(int slot, @NotNull InventoryObject object) {
        Objects.requireNonNull(object, "object");

        if (hasInventoryObject(slot)) {
            throw new IllegalArgumentException("Can't set inventory object in occupied slot");
        }

        InventoryObject old = objects[slot];
        objects[slot] = object;
        return old;
    }

    @Override
    public @NotNull InventoryObject removeInventoryObject(int slot) {
        if (!hasInventoryObject(slot)) {
            throw new IllegalArgumentException("Can't remove inventory object from unoccupied slot");
        }

        InventoryObject old = objects[slot];
        objects[slot] = null;
        return old;
    }

    @Override
    public int getSlotCount() {
        return objects.length;
    }

    @Override
    public @NotNull Iterable<? extends InventoryObject> objects() {
        return Arrays.stream(objects).filter(Objects::nonNull).toList();
    }
}
