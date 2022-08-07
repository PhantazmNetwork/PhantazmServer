package com.github.phantazmnetwork.core.inventory;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Basic implementation of an {@link InventoryProfile}.
 */
public class BasicInventoryProfile implements InventoryProfile {

    private final Map<UUID, InventoryObjectGroup> groups;

    private final InventoryObject[] objects;

    /**
     * Creates a basic inventory profile.
     *
     * @param groups    A map of {@link UUID}s to {@link InventoryObjectGroup}s that this profile holds
     * @param slotCount The number of slots held by the profile (indexed by 0)
     */
    public BasicInventoryProfile(@NotNull Map<UUID, InventoryObjectGroup> groups, int slotCount) {
        this.groups = Map.copyOf(groups);
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

    @Override
    public @NotNull InventoryObjectGroup getGroup(@NotNull Key key) {
        //TODO: implement this API
        throw new UnsupportedOperationException("Not implemented yet");
    }

}
