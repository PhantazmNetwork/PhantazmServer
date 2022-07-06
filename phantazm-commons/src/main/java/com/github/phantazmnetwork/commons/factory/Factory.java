package com.github.phantazmnetwork.commons.factory;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Factory<TIn, TOut> {
    @NotNull TOut make(@NotNull DependencyProvider provider, @NotNull TIn data);
}