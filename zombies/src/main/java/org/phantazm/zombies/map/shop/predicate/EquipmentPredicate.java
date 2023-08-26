package org.phantazm.zombies.map.shop.predicate;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.Upgradable;
import org.phantazm.zombies.map.shop.PlayerInteraction;

public interface EquipmentPredicate {
    boolean canUpgrade(@NotNull PlayerInteraction playerInteraction, @NotNull Upgradable upgradeTarget,
        @NotNull Key chosenUpgrade);

    boolean canAdd(@NotNull PlayerInteraction playerInteraction, @NotNull Key equipmentType);
}
