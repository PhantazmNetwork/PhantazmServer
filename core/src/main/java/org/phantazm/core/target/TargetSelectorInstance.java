package org.phantazm.core.target;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A selector that attempts to find a specific target.
 *
 * @param <TTarget> The type of the target to select
 */
@FunctionalInterface
public interface TargetSelectorInstance<TTarget> {

    /**
     * Selects a target.
     *
     * @return An {@link Optional} of the target that is empty if no target could be found
     */
    @NotNull Optional<TTarget> selectTarget();

}
