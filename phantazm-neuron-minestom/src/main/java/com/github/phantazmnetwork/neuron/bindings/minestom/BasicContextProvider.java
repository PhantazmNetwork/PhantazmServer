package com.github.phantazmnetwork.neuron.bindings.minestom;

import com.github.phantazmnetwork.neuron.engine.*;
import com.github.phantazmnetwork.neuron.world.Collider;
import com.github.phantazmnetwork.neuron.world.SpatialCollider;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.instance.InstanceUnregisterEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;

public class BasicContextProvider implements ContextProvider {
    private final Map<Instance, PathContext> contextMap;

    private final int instanceThreads;
    private final int instanceCache;

    public BasicContextProvider(int instanceThreads, int instanceCache) {
        this.contextMap = new WeakHashMap<>();
        this.instanceThreads = instanceThreads;
        this.instanceCache = instanceCache;
    }

    @Override
    public @NotNull PathContext provideContext(@NotNull Instance instance) {
        synchronized (contextMap) {
            return contextMap.computeIfAbsent(instance, newInstance -> {
                PathEngine engine = new BasicPathEngine(Executors.newFixedThreadPool(instanceThreads));
                Collider collider = new SpatialCollider(new InstanceSpace(newInstance));
                PathCache cache = new BasicPathCache(instanceCache);
                return new BasicPathContext(engine, collider, cache);
            });
        }
    }
}
