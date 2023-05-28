package org.phantazm.mob.target;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;

import java.util.Optional;

public interface TargetSelector<TTarget> {

    /**
     * Selects a target.
     *
     * @return An {@link Optional} of the target that is empty if no target could be found
     */
    @NotNull Optional<TTarget> selectTarget(@NotNull PhantazmMob self);

}
