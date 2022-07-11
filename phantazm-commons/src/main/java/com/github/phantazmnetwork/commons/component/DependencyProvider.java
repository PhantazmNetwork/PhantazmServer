package com.github.phantazmnetwork.commons.component;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public interface DependencyProvider {
    <TDependency> TDependency provide(@NotNull Key key);

    boolean prepare(@NotNull Iterable<? extends Key> dependencies);
}
