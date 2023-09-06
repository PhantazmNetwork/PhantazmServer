package org.phantazm.zombies.equipment.perk.equipment.interactor;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;

public interface PerkInteractorCreator {
    @NotNull PerkInteractor forPlayer(@NotNull ZombiesPlayer zombiesPlayer, @NotNull InjectionStore injectionStore);
}
