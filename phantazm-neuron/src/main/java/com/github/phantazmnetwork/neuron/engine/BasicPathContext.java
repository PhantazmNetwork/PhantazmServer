package com.github.phantazmnetwork.neuron.engine;

import com.github.phantazmnetwork.neuron.world.Collider;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record BasicPathContext(@NotNull PathEngine getEngine, @NotNull Collider getCollider,
                               @NotNull PathCache getCache) implements PathContext {
    public BasicPathContext {
        Objects.requireNonNull(getEngine, "getEngine");
        Objects.requireNonNull(getCollider, "getCollider");
        Objects.requireNonNull(getCache, "getCache");
    }
}
