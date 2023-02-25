package org.phantazm.zombies.equipment.perk.level;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

public interface PerkLevelCreator {
    @NotNull PerkLevel forPlayer(@NotNull ZombiesPlayer zombiesPlayer);

    @NotNull Key levelKey();
}
