package com.github.phantazmnetwork.mob.target;

import com.github.phantazmnetwork.mob.PhantazmMob;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

/**
 * Creates {@link TargetSelectorInstance}s from an associated {@link PhantazmMob}
 * @param <TTarget> The type of the target to select
 */
public interface TargetSelector<TTarget> extends Keyed {

    /**
     * Creates a {@link TargetSelectorInstance}
     * @param mob The {@link PhantazmMob} associated with the selector
     * @return The new {@link TargetSelectorInstance}
     */
    @NotNull TargetSelectorInstance<TTarget> createSelector(@NotNull PhantazmMob mob);

}
