package com.github.phantazmnetwork.zombies.equipment;

import com.github.phantazmnetwork.core.inventory.InventoryAccess;
import com.github.phantazmnetwork.core.inventory.InventoryObjectGroup;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

// TODO: better name
public class EquipmentHandler {

    private final InventoryAccess access;

    public EquipmentHandler(@NotNull InventoryAccess access) {
        this.access = Objects.requireNonNull(access, "access");
    }

    public void addEquipment(@NotNull Equipment equipment, @NotNull Key groupKey) {
        Objects.requireNonNull(equipment, "equipment");
        Objects.requireNonNull(groupKey, "groupKey");

        InventoryObjectGroup group = access.groups().get(groupKey);
        if (group == null) {
            throw new IllegalArgumentException("No group with key " + groupKey);

        }

        if (!canAddEquipment(groupKey)) {
            throw new IllegalArgumentException("Cannot add equipment to group " + groupKey);
        }

        group.pushInventoryObject(equipment);
    }

    public boolean canAddEquipment(@NotNull Key groupKey) {
        Objects.requireNonNull(groupKey, "groupKey");

        InventoryObjectGroup group = access.groups().get(groupKey);
        if (group == null) {
            throw new IllegalArgumentException("No group with key " + groupKey);
        }
        return !group.isFull();
    }

}
