package org.phantazm.zombies.equipment.perk.level;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.equipment.perk.effect.PerkEffect;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Collections;
import java.util.List;

public class EmptyLevelInjector implements PerkLevelInjector {
    @Override
    public void inject(InjectionStore.@NotNull Builder builder, @NotNull ZombiesPlayer zombiesPlayer, @NotNull InjectionStore oldStore) {

    }

    @Override
    public @NotNull List<PerkEffect> makeDefaultEffects(@NotNull ZombiesPlayer zombiesPlayer, @NotNull InjectionStore injectionStore) {
        return Collections.emptyList();
    }

}
