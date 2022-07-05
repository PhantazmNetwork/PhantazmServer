package com.github.phantazmnetwork.commons.factory;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public interface Factory<TReturn> {
    TReturn make(@NotNull Keyed data);

    void register(@NotNull Key type, @NotNull BiFunction<? super Keyed, ? super Factory<TReturn>, ? extends TReturn> factory);

    <TDependency> TDependency getDependency(@NotNull Key dependency);
}
