package com.github.phantazmnetwork.commons.component;

import com.github.steanky.ethylene.core.ConfigElement;
import org.jetbrains.annotations.NotNull;

public interface ComponentBuilder {
    void registerComponentClass(@NotNull Class<?> component);

    <TComponent> TComponent makeComponent(@NotNull ConfigElement element, @NotNull DependencyProvider provider);
}
