package org.phantazm.core.equipment;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.inventory.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class EquipmentHandler {
    private final InventoryAccessRegistry accessRegistry;


    public EquipmentHandler(@NotNull InventoryAccessRegistry accessRegistry) {
        this.accessRegistry = Objects.requireNonNull(accessRegistry, "accessRegistry");
    }

    public void addEquipment(@NotNull Equipment equipment, @NotNull Key groupKey) {
        Objects.requireNonNull(equipment, "equipment");
        Objects.requireNonNull(groupKey, "groupKey");

        if (!accessRegistry.hasCurrentAccess()) {
            throw new IllegalStateException("Inventory access registry does not have current access");
        }

        InventoryObjectGroup group = accessRegistry.getCurrentAccess().groups().get(groupKey);
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

        if (!accessRegistry.hasCurrentAccess()) {
            return false;
        }

        InventoryObjectGroup group = accessRegistry.getCurrentAccess().groups().get(groupKey);
        if (group == null) {
            throw new IllegalArgumentException("No group with key " + groupKey);
        }
        return !group.isFull();
    }

    public @NotNull @Unmodifiable Collection<Equipment> getEquipment(@NotNull Key groupKey) {
        if (!accessRegistry.hasCurrentAccess()) {
            return List.of();
        }

        InventoryAccess access = accessRegistry.getCurrentAccess();
        InventoryObjectGroup group = access.groups().get(groupKey);
        IntSet slots = group.getSlots();

        InventoryProfile profile = access.profile();
        List<Equipment> equipmentList = new ArrayList<>(slots.size());
        for (int slot : slots) {
            if (profile.hasInventoryObject(slot)) {
                InventoryObject object = profile.getInventoryObject(slot);
                if (object instanceof Equipment equipment) {
                    equipmentList.add(equipment);
                }
            }

        }

        return List.copyOf(equipmentList);
    }
}
