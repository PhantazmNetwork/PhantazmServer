package com.github.phantazmnetwork.commons.component;

import com.github.steanky.ethylene.core.ConfigElement;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public interface ComponentBuilder {
    void registerComponentClass(@NotNull Class<?> component);

    <TData extends Keyed, TComponent> TComponent makeComponent(@NotNull ConfigElement element,
                                                               @NotNull DependencyProvider provider);
}
