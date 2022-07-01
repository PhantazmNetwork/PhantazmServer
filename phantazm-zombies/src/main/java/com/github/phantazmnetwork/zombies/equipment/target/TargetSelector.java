package com.github.phantazmnetwork.zombies.equipment.target;

import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.api.target.TargetSelectorInstance;
import com.github.phantazmnetwork.mob.MobStore;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public interface TargetSelector<TTarget> extends Keyed {

    @NotNull TargetSelectorInstance<TTarget> createSelector(@NotNull MobStore store, @NotNull PlayerView player);

}
