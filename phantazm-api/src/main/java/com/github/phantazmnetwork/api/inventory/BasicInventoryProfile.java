package com.github.phantazmnetwork.api.inventory;

import net.minestom.server.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Basic implementation of an {@link InventoryProfile}.
 */
public class BasicInventoryProfile implements InventoryProfile {

    private final InventoryObject[] objects = new InventoryObject[PlayerInventory.INVENTORY_SIZE];

    private boolean visible = false;

    @Override
    public @Nullable InventoryObject getInventoryObject(int slot) {
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

        inventoryObject.onRemove();
        objects[slot] = null;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        if (this.visible != visible) {
            this.visible = visible;

            for (InventoryObject inventoryObject : objects) {
                if (inventoryObject != null) {
                    inventoryObject.setVisible(visible);
                }
            }
        }
    }

}
