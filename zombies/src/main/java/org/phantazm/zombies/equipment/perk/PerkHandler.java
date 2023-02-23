package org.phantazm.zombies.equipment.perk;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

public interface PerkHandler {
    @NotNull Perk forPlayer(@NotNull Key perkType, @NotNull ZombiesPlayer zombiesPlayer);

    boolean hasType(@NotNull Key perkType);
}
