package org.phantazm.zombies.equipment.perk.level;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.equipment.perk.effect.PerkEffect;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.List;

public interface PerkLevelInjector {

    void inject(@NotNull InjectionStore.Builder builder, @NotNull ZombiesPlayer zombiesPlayer, @NotNull InjectionStore oldStore);

    @NotNull List<PerkEffect> makeDefaultEffects(@NotNull ZombiesPlayer zombiesPlayer, @NotNull InjectionStore injectionStore);

}
