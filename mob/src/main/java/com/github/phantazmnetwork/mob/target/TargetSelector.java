package com.github.phantazmnetwork.mob.target;

import com.github.phantazmnetwork.core.target.TargetSelectorInstance;
import com.github.phantazmnetwork.mob.PhantazmMob;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Creates {@link TargetSelectorInstance}s from an associated {@link PhantazmMob}
 *
 * @param <TTarget> The type of the target to select
 */
public interface TargetSelector<TTarget> {

    /**
     * Selects a target.
     *
     * @return An {@link Optional} of the target that is empty if no target could be found
     */
    @NotNull Optional<TTarget> selectTarget();

}
