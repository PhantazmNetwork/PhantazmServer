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
 * {@link PathContext} objects and {@link ExecutorService}s used to asynchronously run {@link PathOperation}s.</p>
 *
 * <p>When instances are unloaded (unregistered), the corresponding ExecutorService will be terminated.</p>
 *
 * <p>This class is expected to be singleton. It should be initialized before server startup, as it registers an
 * event internally to listen for instance removal.</p>
 */
public class BasicContextProvider implements ContextProvider {
    private final Map<Instance, Pair<ExecutorService, PathContext>> contextMap;

    private final int instanceThreads;
    private final int instanceCache;

    /**
     * Creates a new instance of this class.
     * @param node the {@link EventNode} to listen for {@link InstanceUnregisterEvent}s on
     * @param instanceThreads the number of threads to maintain per-instance for pathfinding
     * @param instanceCache the maximum size of the {@link PathCache} maintained for each instance
     */
    public BasicContextProvider(@NotNull EventNode<Event> node, int instanceThreads, int instanceCache) {
        this.contextMap = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
        this.instanceThreads = instanceThreads;
        this.instanceCache = instanceCache;

        node.addListener(InstanceUnregisterEvent.class, this::onInstanceUnregister);
    }

    @Override
    public @NotNull PathContext provideContext(@NotNull Instance instance) {
        return contextMap.computeIfAbsent(instance, newInstance -> {
            ExecutorService service = Executors.newFixedThreadPool(instanceThreads);

            PathEngine engine = new BasicPathEngine(service);
            Collider collider = new SpatialCollider(new InstanceSpace(newInstance));
            PathCache cache = new BasicPathCache(instanceCache);
            return Pair.of(service, new BasicPathContext(engine, collider, cache));
        }).right();
    }

    private void onInstanceUnregister(@NotNull InstanceUnregisterEvent event) {
        Pair<ExecutorService, PathContext> context = contextMap.remove(event.getInstance());

        if(context != null) {
            context.left().shutdown();
        }
    }
}
