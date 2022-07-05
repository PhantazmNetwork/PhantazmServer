package com.github.phantazmnetwork.commons.factory;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BasicFactory<TReturn> implements Factory<TReturn> {
    private final Function<Key, ?> dependencyFunction;
    private final Map<Key, BiFunction<? super Keyed, ? super Factory<TReturn>, ? extends TReturn>> constructors;


    public BasicFactory(@NotNull Function<Key, ?> dependencyFunction, int initialCapacity) {
        this.dependencyFunction = Objects.requireNonNull(dependencyFunction);
        this.constructors = new HashMap<>(initialCapacity);
    }

    @Override
    public TReturn make(@NotNull Keyed data) {
        Key key = data.key();
        BiFunction<? super Keyed, ? super Factory<TReturn>, ? extends TReturn> constructor = constructors.get(key);
        if(constructor == null) {
            throw new IllegalArgumentException("Unable to find constructor for data with key " + key);
        }

        return constructor.apply(data, this);
    }

    @Override
    public void register(@NotNull Key type, @NotNull BiFunction<? super Keyed, ? super Factory<TReturn>, ? extends TReturn> factory) {
        constructors.put(type, factory);
    }

    @Override
    public <TDependency> TDependency getDependency(@NotNull Key dependency) {
        //noinspection unchecked
        return (TDependency) dependencyFunction.apply(dependency);
    }
}
