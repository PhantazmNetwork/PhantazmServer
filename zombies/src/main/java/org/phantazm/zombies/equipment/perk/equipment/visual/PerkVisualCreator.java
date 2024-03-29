package org.phantazm.zombies.equipment.perk.equipment.visual;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

public interface PerkVisualCreator {
    @NotNull PerkVisual forPlayer(@NotNull ZombiesPlayer zombiesPlayer);
}
