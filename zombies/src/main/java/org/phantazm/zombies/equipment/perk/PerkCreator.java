package org.phantazm.zombies.equipment.perk;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;

public interface PerkCreator {
    @NotNull Perk forPlayer(@NotNull ZombiesPlayer zombiesPlayer, @NotNull InjectionStore injectionStore);
}
