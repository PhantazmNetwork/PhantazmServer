package com.github.phantazmnetwork.mob.target;

import com.github.phantazmnetwork.api.target.TargetSelectorInstance;
import com.github.phantazmnetwork.mob.PhantazmMob;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link TargetSelector} that maps target types.
 * @param <TFrom> The delegate {@link TargetSelector}'s target type
 * @param <TTo> The new {@link TargetSelector}'s target type
 */
public abstract class MappedSelector<TFrom, TTo> implements TargetSelector<TTo> {

    private final TargetSelector<TFrom> delegate;

    /**
     * Creates a new {@link MappedSelector}.
     * @param delegate The delegate {@link TargetSelector} to map
     */
    public MappedSelector(@NotNull TargetSelector<TFrom> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    /**
     * Gets the delegate {@link TargetSelector}.
     * @return The delegate {@link TargetSelector}
     */
    public @NotNull TargetSelector<TFrom> getDelegate() {
        return delegate;
    }

    @Override
    public @NotNull Key getSerialKey() {
        return delegate.getSerialKey();
    }

    @Override
    public @NotNull TargetSelectorInstance<TTo> createSelector(@NotNull PhantazmMob mob) {
        TargetSelectorInstance<TFrom> delegateSelector = delegate.createSelector(mob);
        return () -> delegateSelector.selectTarget().map(this::map);
    }

    /**
     * Maps a {@link TFrom} target to a {@link TTo} target.
     * @param from The {@link TFrom} target to map
     * @return The {@link TTo} target
     */
    protected abstract TTo map(@NotNull TFrom from);

}
