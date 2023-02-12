package org.phantazm.core.equipment;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.inventory.*;

import java.util.*;

public class EquipmentHandler {
    private final InventoryAccessRegistry accessRegistry;


    public EquipmentHandler(@NotNull InventoryAccessRegistry accessRegistry) {
        this.accessRegistry = Objects.requireNonNull(accessRegistry, "accessRegistry");
    }

    public void addEquipment(@NotNull Equipment equipment, @NotNull Key groupKey) {
        Objects.requireNonNull(equipment, "equipment");
        Objects.requireNonNull(groupKey, "groupKey");

        Optional<InventoryAccess> accessOptional = accessRegistry.getCurrentAccess();
        if (accessOptional.isEmpty()) {
            throw new IllegalStateException("Inventory access registry does not have current access");
        }

        InventoryAccess currentAccess = accessOptional.get();

        InventoryObjectGroup group = currentAccess.groups().get(groupKey);
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

        Optional<InventoryAccess> accessOptional = accessRegistry.getCurrentAccess();

        if (accessOptional.isEmpty()) {
            return false;
        }

        InventoryObjectGroup group = accessOptional.get().groups().get(groupKey);
        if (group == null) {
            return false;
        }

        return !group.isFull();
    }

    public @NotNull @Unmodifiable Collection<Equipment> getEquipment(@NotNull Key groupKey) {
        Optional<InventoryAccess> accessOptional = accessRegistry.getCurrentAccess();
        if (accessOptional.isEmpty()) {
            return List.of();
        }

        InventoryAccess access = accessOptional.get();
        InventoryObjectGroup group = access.groups().get(groupKey);
        if (group == null) {
            return List.of();
        }

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
