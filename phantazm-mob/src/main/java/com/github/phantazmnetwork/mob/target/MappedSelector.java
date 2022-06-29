package com.github.phantazmnetwork.mob.target;

import com.github.phantazmnetwork.mob.PhantazmMob;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class MappedSelector<TFrom, TTo> implements TargetSelector<TTo> {

    private final TargetSelector<TFrom> delegate;

    public MappedSelector(@NotNull TargetSelector<TFrom> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    public @NotNull TargetSelector<TFrom> getDelegate() {
        return delegate;
    }

    @Override
    public @NotNull Key key() {
        return delegate.key();
    }

    @Override
    public @NotNull TargetSelectorInstance<TTo> createSelector(@NotNull PhantazmMob mob) {
        TargetSelectorInstance<TFrom> delegateSelector = delegate.createSelector(mob);
        return () -> delegateSelector.selectTarget().map(this::map);
    }

    protected abstract TTo map(@NotNull TFrom from);

}
