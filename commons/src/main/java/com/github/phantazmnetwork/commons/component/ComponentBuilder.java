package com.github.phantazmnetwork.commons.component;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.function.IntFunction;

/**
 * Represents a builder and registry of component objects. These are objects that are generally loaded from some
 * identifiable data ({@link Keyed} objects).
 */
public interface ComponentBuilder {
    interface ComponentConsumer<V> {
        void accept(V v) throws ComponentException;
    }

    /**
     * Registers the provided component class. The class must conform to the standard component model. Classes may only
     * be registered once. Once a class has been registered, it may be freely instantiated using
     * {@link ComponentBuilder#makeComponent(ConfigNode, DependencyProvider)} and appropriate data.
     *
     * @param component the component class to register
     */
    void registerComponentClass(@NotNull Class<?> component) throws ComponentException;

    /**
     * Creates a component object from the given data {@link ConfigNode} and {@link DependencyProvider}.
     *
     * @param node         the node used to define the object's data
     * @param provider     the DependencyProvider used to provide arbitrary data to the factory
     * @param <TComponent> the component object type
     * @return the component
     */
    <TComponent> TComponent makeComponent(@NotNull ConfigNode node, @NotNull DependencyProvider provider)
            throws ComponentException;

    default <TComponent, TCollection extends Collection<TComponent>> @NotNull TCollection makeComponents(
            @NotNull ConfigList list, @NotNull DependencyProvider provider,
            @NotNull IntFunction<TCollection> collectionIntFunction,
            @NotNull ComponentConsumer<? super ComponentException> exceptionHandler) throws ComponentException {
        TCollection out = collectionIntFunction.apply(list.size());
        ComponentException root = null;
        for (ConfigElement element : list) {
            if (element.isNode()) {
                try {
                    out.add(makeComponent(element.asNode(), provider));
                }
                catch (ComponentException e) {
                    if (root == null) {
                        root = e;
                    }
                    else {
                        root.addSuppressed(e);
                    }
                }
            }
        }

        if (root != null) {
            exceptionHandler.accept(root);
        }

        return out;
    }

    default <TComponent, TCollection extends Collection<TComponent>> @NotNull TCollection makeComponents(
            @NotNull ConfigList list, @NotNull DependencyProvider provider,
            @NotNull IntFunction<TCollection> collectionIntFunction) throws ComponentException {
        return makeComponents(list, provider, collectionIntFunction, e -> {
            throw e;
        });
    }
}
