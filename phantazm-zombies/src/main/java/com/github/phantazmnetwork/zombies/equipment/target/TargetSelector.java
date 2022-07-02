package com.github.phantazmnetwork.zombies.equipment.target;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.api.target.TargetSelectorInstance;
import com.github.phantazmnetwork.mob.MobStore;
import org.jetbrains.annotations.NotNull;

public interface TargetSelector<TTarget> extends VariantSerializable {

    @NotNull TargetSelectorInstance<TTarget> createSelector(@NotNull MobStore store, @NotNull PlayerView playerView);

}
