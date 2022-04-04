package com.github.phantazmnetwork.neuron.engine;

import com.github.phantazmnetwork.neuron.world.Collider;
import org.jetbrains.annotations.NotNull;

public interface PathContext {
    @NotNull PathEngine getEngine();

    @NotNull Collider getCollider();

    @NotNull PathCache getCache();
}
