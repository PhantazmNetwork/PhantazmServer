package org.phantazm.core.equipment;

import com.github.steanky.toolkit.collection.Wrapper;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.inventory.*;

import java.util.*;
import java.util.function.Supplier;

public class EquipmentHandler {
    private final InventoryAccessRegistry accessRegistry;

    public EquipmentHandler(@NotNull InventoryAccessRegistry accessRegistry) {
        this.accessRegistry = Objects.requireNonNull(accessRegistry, "accessRegistry");
    }

    public @NotNull InventoryAccessRegistry accessRegistry() {
        return accessRegistry;
    }

    public void addEquipment(@NotNull Equipment equipment, @NotNull Key groupKey) {
        Objects.requireNonNull(equipment, "equipment");
        Objects.requireNonNull(groupKey, "groupKey");

        if (!accessRegistry.canPushTo(groupKey)) {
            throw new IllegalArgumentException("Can't push to group " + groupKey);
        }

        accessRegistry.pushObject(groupKey, equipment);
    }

    public boolean hasEquipment(@NotNull Key equipmentGroup, @NotNull Key equipmentKey) {
        Wrapper<Boolean> result = Wrapper.of(false);
        accessRegistry.getCurrentAccess().ifPresent(access -> {
            InventoryObjectGroup group = access.groups().get(equipmentGroup);
            if (group != null) {
                InventoryProfile profile = group.getProfile();
                for (int slot : group.getSlots()) {
                    if (profile.hasInventoryObject(slot)) {
                        InventoryObject object = profile.getInventoryObject(slot);
                        if (object instanceof Equipment equipment && equipment.key().equals(equipmentKey)) {
                            result.set(true);
                            return;
                        }
                    }
                }
            }
        });

        return result.get();
    }

    public boolean canAddEquipment(@NotNull Key groupKey) {
        Objects.requireNonNull(groupKey, "groupKey");

        return accessRegistry.canPushTo(groupKey);
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

    public void refreshGroup(@NotNull Key groupKey) {
        accessRegistry.getCurrentAccess().ifPresent(access -> {
            InventoryObjectGroup group = access.groups().get(groupKey);
            InventoryObject defaultObject;
            if (group == null || (defaultObject = group.defaultObject()) == null) {
                return;
            }

            InventoryProfile profile = access.profile();
            IntSet slots = group.getSlots();
            for (int slot : slots) {
                if (!profile.hasInventoryObject(slot)) {
                    profile.setInventoryObject(slot, defaultObject);
                }
            }
        });
    }

    public @NotNull Result addOrReplaceEquipment(Key groupKey, Key equipmentKey, boolean allowReplace, int specificSlot,
            boolean allowDuplicate, @NotNull Supplier<? extends Optional<? extends Equipment>> equipmentSupplier,
            @Nullable Player player) {
        if (!allowDuplicate && hasEquipment(groupKey, equipmentKey)) {
            return Result.DUPLICATE;
        }

        boolean addEquipment;
        if (!(addEquipment = canAddEquipment(groupKey)) && !allowReplace) {
            return Result.FAILED;
        }

        InventoryAccessRegistry accessRegistry = accessRegistry();
        Optional<InventoryAccess> inventoryAccessOptional = accessRegistry.getCurrentAccess();
        if (inventoryAccessOptional.isEmpty()) {
            return Result.FAILED;
        }

        InventoryAccess inventoryAccess = inventoryAccessOptional.get();
        if (addEquipment) {
            Optional<? extends Equipment> equipmentOptional = equipmentSupplier.get();
            if (equipmentOptional.isEmpty()) {
                return Result.FAILED;
            }

            Equipment equipment = equipmentOptional.get();
            if (specificSlot < 0) {
                addEquipment(equipment, groupKey);
                return Result.ADDED;
            }

            InventoryObjectGroup group = inventoryAccess.groups().get(groupKey);
            if (invalidGroupSlot(group, specificSlot)) {
                return Result.FAILED;
            }

            InventoryProfile profile = group.getProfile();
            if (!profile.hasInventoryObject(specificSlot)) {
                accessRegistry.replaceObject(specificSlot, equipment);
                return Result.ADDED;
            }

            InventoryObject currentObject = profile.getInventoryObject(specificSlot);
            if (allowReplace || group.defaultObject() == currentObject) {
                InventoryObject old = accessRegistry.replaceObject(specificSlot, equipment);
                return old == null || old == group.defaultObject() ? Result.ADDED : Result.REPLACED;
            }

            return Result.FAILED;
        }

        if (player == null) {
            return Result.FAILED;
        }

        int targetSlot = specificSlot < 0 ? player.getHeldSlot() : specificSlot;

        InventoryObjectGroup group = inventoryAccess.groups().get(groupKey);
        if (invalidGroupSlot(group, targetSlot)) {
            return Result.FAILED;
        }

        Optional<? extends Equipment> equipmentOptional = equipmentSupplier.get();
        if (equipmentOptional.isEmpty()) {
            return Result.FAILED;
        }

        Equipment equipment = equipmentOptional.get();
        InventoryObject old = accessRegistry.replaceObject(targetSlot, equipment);
        return old == null || old == group.defaultObject() ? Result.ADDED : Result.REPLACED;
    }

    private boolean invalidGroupSlot(InventoryObjectGroup group, int slot) {
        return group == null || !group.getSlots().contains(slot);
    }

    public enum Result {
        ADDED,
        REPLACED,
        DUPLICATE,
        FAILED;

        public boolean success() {
            return this == ADDED || this == REPLACED;
        }
    }
}
