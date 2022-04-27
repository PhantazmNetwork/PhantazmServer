package com.github.phantazmnetwork.neuron.bindings.minestom;

import com.github.phantazmnetwork.neuron.engine.*;
import com.github.phantazmnetwork.neuron.world.Collider;
import com.github.phantazmnetwork.neuron.world.SpatialCollider;
import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.InstanceUnregisterEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BasicContextProvider implements ContextProvider {
    private final Map<Instance, Pair<ExecutorService, PathContext>> contextMap;

    private final int instanceThreads;
    private final int instanceCache;

    public BasicContextProvider(@NotNull EventNode<Event> node, int instanceThreads, int instanceCache) {
        this.contextMap = new WeakHashMap<>();
        this.instanceThreads = instanceThreads;
        this.instanceCache = instanceCache;

        node.addListener(InstanceUnregisterEvent.class, this::onInstanceUnregister);
    }

    @Override
    public @NotNull PathContext provideContext(@NotNull Instance instance) {
        synchronized (contextMap) {
            return contextMap.computeIfAbsent(instance, newInstance -> {
                ExecutorService service = Executors.newFixedThreadPool(instanceThreads);

                PathEngine engine = new BasicPathEngine(service);
                Collider collider = new SpatialCollider(new InstanceSpace(newInstance));
                PathCache cache = new BasicPathCache(instanceCache);
                return Pair.of(service, new BasicPathContext(engine, collider, cache));
            }).right();
        }
    }

    private void onInstanceUnregister(@NotNull InstanceUnregisterEvent event) {
        Pair<ExecutorService, PathContext> context;
        synchronized (contextMap) {
            context = contextMap.remove(event.getInstance());
        }

        if(context != null) {
            context.left().shutdown();
        }
    }
}
