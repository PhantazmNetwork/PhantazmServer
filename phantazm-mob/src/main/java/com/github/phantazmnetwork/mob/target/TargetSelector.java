package com.github.phantazmnetwork.mob.target;

import com.github.phantazmnetwork.mob.PhantazmMob;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public interface TargetSelector<TTarget> extends Keyed {

    @NotNull TargetSelectorInstance<TTarget> createSelector(@NotNull PhantazmMob mob);

}
