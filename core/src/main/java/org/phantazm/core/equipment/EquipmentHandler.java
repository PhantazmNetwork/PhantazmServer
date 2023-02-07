package org.phantazm.core.equipment;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.inventory.InventoryAccessRegistry;
import org.phantazm.core.inventory.InventoryObjectGroup;

import java.util.Objects;

// TODO: better name
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

}
