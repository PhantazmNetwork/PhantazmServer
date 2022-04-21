package com.github.phantazmnetwork.neuron.bindings.minestom;

import com.github.phantazmnetwork.neuron.engine.*;
import com.github.phantazmnetwork.neuron.world.Collider;
import com.github.phantazmnetwork.neuron.world.SpatialCollider;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;

public class BasicContextProvider implements ContextProvider {
    //TODO base this on a config value or # of CPU cores
    private static final int THREADS_PER_INSTANCE = 4;

    //TODO base this on a config value
    private static final int CACHE_SIZE_PER_INSTANCE = 1024;

    private final Map<Instance, PathContext> contextMap;

    public BasicContextProvider() {
        this.contextMap = new WeakHashMap<>();
    }

    @Override
    public @NotNull PathContext provideContext(@NotNull Instance instance) {
        return contextMap.computeIfAbsent(instance, (key) -> {
            //TODO: when instance is unregistered, shut down ExecutorService
            PathEngine engine = new BasicPathEngine(Executors.newFixedThreadPool(THREADS_PER_INSTANCE));
            Collider collider = new SpatialCollider(new InstanceSpace(instance));
            PathCache cache = new BasicPathCache(CACHE_SIZE_PER_INSTANCE);
            return new BasicPathContext(engine, collider, cache);
        });
    }
}
