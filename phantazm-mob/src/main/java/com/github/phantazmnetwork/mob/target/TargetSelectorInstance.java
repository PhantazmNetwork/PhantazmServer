package com.github.phantazmnetwork.mob.target;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@FunctionalInterface
public interface TargetSelectorInstance<TTarget> {

    @NotNull Optional<TTarget> selectTarget();

}
