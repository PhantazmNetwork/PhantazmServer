package org.phantazm.zombies.equipment;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface EquipmentCreator {

    boolean hasEquipment(@NotNull Key equipmentKey);

    @NotNull <TEquipment extends Equipment> Optional<TEquipment> createEquipment(@NotNull Key equipmentKey);

}
