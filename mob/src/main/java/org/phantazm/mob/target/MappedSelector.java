package org.phantazm.mob.target;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;

import java.util.Objects;
import java.util.Optional;

/**
 * A {@link TargetSelector} that maps target types.
 *
 * @param <TFrom> The delegate {@link TargetSelector}'s target type
 * @param <TTo>   The new {@link TargetSelector}'s target type
 */
public abstract class MappedSelector<TFrom, TTo> implements TargetSelector<TTo> {

    private final TargetSelector<TFrom> delegate;

    /**
     * Creates a new {@link MappedSelector}.
     *
     * @param delegate The delegate {@link TargetSelector} to map
     */
    public MappedSelector(@NotNull TargetSelector<TFrom> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    /**
     * Gets the delegate {@link TargetSelector}.
     *
     * @return The delegate {@link TargetSelector}
     */
    public @NotNull TargetSelector<TFrom> getDelegate() {
        return delegate;
    }

    @Override
    public @NotNull Optional<TTo> selectTarget(@NotNull PhantazmMob self) {
        return delegate.selectTarget(self).map(this::map);
    }

    /**
     * Maps a {@link TFrom} target to a {@link TTo} target.
     *
     * @param from The {@link TFrom} target to map
     * @return The {@link TTo} target
     */
    protected abstract TTo map(@NotNull TFrom from);

}
