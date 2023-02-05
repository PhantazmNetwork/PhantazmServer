package org.phantazm.zombies.map.shop.predicate;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.upgrade.Upgradable;

public interface EquipmentPredicate {
    boolean canUpgrade(@NotNull PlayerInteraction playerInteraction, @NotNull Upgradable upgradeTarget,
            @NotNull Key chosenUpgrade);

    boolean canAdd(@NotNull PlayerInteraction playerInteraction, @NotNull Key equipmentType);
}
