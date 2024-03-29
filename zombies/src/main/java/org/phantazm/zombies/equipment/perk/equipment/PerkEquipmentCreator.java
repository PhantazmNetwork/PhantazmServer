package org.phantazm.zombies.equipment.perk.equipment;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

public interface PerkEquipmentCreator {
    @NotNull PerkEquipment forPlayer(@NotNull ZombiesPlayer zombiesPlayer);
}
