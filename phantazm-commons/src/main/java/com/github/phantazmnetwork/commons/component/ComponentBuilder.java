package com.github.phantazmnetwork.commons.component;

import com.github.steanky.ethylene.core.collection.ConfigNode;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a builder and registry of component objects. These are objects that are generally loaded from some
 * identifiable data ({@link Keyed} objects).
 */
public interface ComponentBuilder {
    /**
     * Registers the provided component class. The class must conform to the standard component model. Classes may only
     * be registered once. Once a class has been registered, it may be freely instantiated using
     * {@link ComponentBuilder#makeComponent(ConfigNode, DependencyProvider)} and appropriate data.
     * @param component the component class to register
     */
    void registerComponentClass(@NotNull Class<?> component);

    /**
     * Creates a component object from the given data {@link ConfigNode} and {@link DependencyProvider}.
     * @param node the node used to define the object's data
     * @param provider the DependencyProvider used to provide arbitrary data to the factory
     * @return the component
     * @param <TData> the type of data accepted by the component's factory, defined for use by implementations
     * @param <TComponent> the component object type
     */
    <TData extends Keyed, TComponent> TComponent makeComponent(@NotNull ConfigNode node,
                                                               @NotNull DependencyProvider provider);
}
