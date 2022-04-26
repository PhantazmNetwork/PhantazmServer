package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.neuron.bindings.minestom.BasicContextProvider;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.ContextualSpawner;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.Spawner;
import com.github.phantazmnetwork.server.config.server.PathfinderConfig;
import org.jetbrains.annotations.NotNull;

public final class Neuron {
    private static Spawner spawner;

    private Neuron() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull PathfinderConfig pathfinderConfig) {
        spawner = new ContextualSpawner(new BasicContextProvider(pathfinderConfig.threads(), pathfinderConfig
                .cacheSize()));
    }

    public static @NotNull Spawner getSpawner() {
        if(spawner == null) {
            throw new IllegalStateException("Pathfinding has not been initialized yet");
        }

        return spawner;
    }
}
