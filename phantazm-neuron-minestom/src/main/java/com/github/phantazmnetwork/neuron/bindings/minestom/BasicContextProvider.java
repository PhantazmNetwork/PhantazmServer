package com.github.phantazmnetwork.neuron.bindings.minestom;

import com.github.phantazmnetwork.neuron.engine.*;
import com.github.phantazmnetwork.neuron.operation.PathOperation;
import com.github.phantazmnetwork.neuron.world.Collider;
import com.github.phantazmnetwork.neuron.world.SpatialCollider;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.InstanceUnregisterEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>A basic implementation of {@link ContextProvider}. Maintains a 1:1 correspondence of {@link Instance} objects to
 * {@link PathContext} objects.</p>
 *
 * <p>This class is expected to be singleton.</p>
 */
public class BasicContextProvider implements ContextProvider {
    private final Map<Instance, PathContext> contextMap;

    private final ExecutorService executorService;

    private final int instanceCache;

    /**
     * Creates a new instance of this class.
     * @param threads the number of threads to maintain for pathfinding
     * @param instanceCache the maximum size of the {@link PathCache} maintained for each instance
     */
    public BasicContextProvider(int threads, int instanceCache) {
        this.contextMap = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
        this.instanceCache = instanceCache;

        this.executorService = Executors.newWorkStealingPool(threads);
    }

    @Override
    public @NotNull PathContext provideContext(@NotNull Instance instance) {
        return contextMap.computeIfAbsent(instance, newInstance -> {
            PathEngine engine = new BasicPathEngine(executorService);
            Collider collider = new SpatialCollider(new InstanceSpace(newInstance));
            PathCache cache = new BasicPathCache(instanceCache);
            return new BasicPathContext(engine, collider, cache);
        });
    }
}
