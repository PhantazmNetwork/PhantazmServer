package com.github.phantazmnetwork.zombies.equipment;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@FunctionalInterface
public interface EquipmentCreator {

    @NotNull <TEquipment extends Equipment> Optional<TEquipment> createEquipment(@NotNull Key equipmentKey);

}
