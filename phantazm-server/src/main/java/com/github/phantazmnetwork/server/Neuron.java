package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.neuron.bindings.minestom.BasicContextProvider;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.ContextualSpawner;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.Spawner;
import com.github.phantazmnetwork.server.config.server.PathfinderConfig;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;

/**
 * Main entrypoint for Neuron-related features.
 */
public final class Neuron {
    private static Spawner spawner;

    private Neuron() {
        throw new UnsupportedOperationException();
    }

    /**
     * Initializes pathfinding-related features. Should only be called once from {@link PhantazmServer#main(String[])}.
     * @param pathfinderConfig the {@link PathfinderConfig} instance used to configure pathfinding behavior
     */
    static void initialize(@NotNull EventNode<Event> globalNode, @NotNull PathfinderConfig pathfinderConfig) {
        spawner = new ContextualSpawner(new BasicContextProvider(globalNode, Executors.newWorkStealingPool(
                pathfinderConfig.threads()), pathfinderConfig.cacheSize(), pathfinderConfig.updateQueueCapacity()));
    }

    /**
     * Returns the global {@link Spawner} used to spawn {@link NeuralEntity} instances.
     * @return the global spawner
     */
    public static @NotNull Spawner getSpawner() {
        if(spawner == null) {
            throw new IllegalStateException("Neuron has not been initialized yet");
        }

        return spawner;
    }
}
