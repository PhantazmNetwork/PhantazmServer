package com.github.phantazmnetwork.commons.component;

import com.github.steanky.ethylene.core.collection.ConfigNode;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
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
     * be registered once.
     *
     * @param component the component class to register
     */
    void registerComponentClass(@NotNull Class<?> component) throws ComponentException;

    @NotNull Keyed makeData(@NotNull ConfigNode node) throws ComponentException;

    /**
     * Creates a component object from the given data {@link ConfigNode} and {@link DependencyProvider}.
     *
     * @param data         the data used to create the object
     * @param provider     the DependencyProvider used to provide arbitrary data to the factory
     * @param <TComponent> the component object type
     * @return the component
     */
    <TComponent> TComponent makeComponent(@NotNull Keyed data, @NotNull DependencyProvider provider)
            throws ComponentException;

    default <TComponent, TCollection extends Collection<TComponent>> @NotNull TCollection makeComponents(
            @NotNull Collection<? extends Keyed> list, @NotNull DependencyProvider provider,
            @NotNull IntFunction<TCollection> collectionIntFunction,
            @NotNull ComponentConsumer<? super ComponentException> exceptionHandler) throws ComponentException {
        TCollection out = collectionIntFunction.apply(list.size());
        ComponentException root = null;
        for (Keyed data : list) {
            try {
                out.add(makeComponent(data, provider));
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

        if (root != null) {
            exceptionHandler.accept(root);
        }

        return out;
    }
}
