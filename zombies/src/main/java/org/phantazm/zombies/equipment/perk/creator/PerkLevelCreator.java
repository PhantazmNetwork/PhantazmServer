package org.phantazm.zombies.equipment.perk.creator;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.perk.PerkLevel;
import org.phantazm.zombies.player.ZombiesPlayer;

public interface PerkLevelCreator {
    @NotNull PerkLevel forPlayer(@NotNull ZombiesPlayer zombiesPlayer);
}
