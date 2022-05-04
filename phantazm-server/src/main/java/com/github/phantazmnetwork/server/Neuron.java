package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.neuron.bindings.minestom.BasicContextProvider;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.ContextualSpawner;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.Spawner;
import com.github.phantazmnetwork.server.config.server.PathfinderConfig;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;

public final class Neuron {
    private static Spawner spawner;

    private Neuron() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull EventNode<Event> node, @NotNull PathfinderConfig pathfinderConfig) {
        spawner = new ContextualSpawner(new BasicContextProvider(node, pathfinderConfig.threads(), pathfinderConfig
                .cacheSize()));
    }

    public static @NotNull Spawner getSpawner() {
        if(spawner == null) {
            throw new IllegalStateException("Neuron has not been initialized yet");
        }

        return spawner;
    }
}
