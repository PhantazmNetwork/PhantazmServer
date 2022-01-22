package com.github.phantazmnetwork.api.inventory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Basic implementation of an {@link InventoryProfile}.
 */
public class BasicInventoryProfile implements InventoryProfile {

    private final InventoryObject[] objects;

    private boolean visible = false;

    /**
     * Creates a basic inventory profile.
     * @param slotCount The number of slots held by the profile (indexed by 0)
     */
    public BasicInventoryProfile(int slotCount) {
        this.objects = new InventoryObject[slotCount];
    }

    @Override
    public InventoryObject getInventoryObject(int slot) {
        return objects[slot];
    }

    @Override
    public void setInventoryObject(int slot, @NotNull InventoryObject object) {
        Objects.requireNonNull(object, "object");

        if (objects[slot] != null) {
            throw new IllegalArgumentException("Can't set inventory object in occupied slot");
        }

        objects[slot] = object;
    }

    @Override
    public void removeInventoryObject(int slot) {
        InventoryObject inventoryObject = objects[slot];
        if (inventoryObject == null) {
            throw new IllegalArgumentException("Can't remove inventory object from unoccupied slot");
        }

        objects[slot] = null;
    }

    @Override
    public int getSlotCount() {
        return objects.length;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        if (this.visible != visible) {
            this.visible = visible;

            for (int i = 0; i < objects.length; i++) {
                InventoryObject object = objects[i];
                if (object != null) {
                    object.updateInInventory(i, visible);
                }
            }
        }
    }

}
