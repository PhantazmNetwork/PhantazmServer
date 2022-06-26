package com.github.phantazmnetwork.mob.target;

import com.github.phantazmnetwork.mob.PhantazmMob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public abstract class MappedSelector<TFrom, TTo> implements TargetSelector<TTo> {

    private final TargetSelector<TFrom> delegate;

    public MappedSelector(@NotNull TargetSelector<TFrom> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    public @NotNull TargetSelector<TFrom> getDelegate() {
        return delegate;
    }

    @Override
    public @NotNull Optional<TTo> selectTarget(@NotNull PhantazmMob mob) {
        return delegate.selectTarget(mob).map(this::map);
    }

    protected abstract TTo map(@NotNull TFrom from);

}
