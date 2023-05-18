package org.phantazm.zombies.equipment.perk.effect;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

public interface PerkEffectCreator {
    @NotNull PerkEffect forPlayer(@NotNull ZombiesPlayer zombiesPlayer);
}
