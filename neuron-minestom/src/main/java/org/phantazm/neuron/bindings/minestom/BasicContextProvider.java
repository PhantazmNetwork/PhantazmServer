package org.phantazm.neuron.bindings.minestom;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.BlockChangeEvent;
import net.minestom.server.event.instance.InstanceUnregisterEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.VecUtils;
import org.phantazm.neuron.bindings.minestom.solid.SolidProvider;
import org.phantazm.neuron.engine.*;
import org.phantazm.neuron.world.SpatialCollider;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;

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
    private final int updateQueueCapacity;

    /**
     * Creates a new instance of this class.
     *
     * @param globalEventNode     the event node to which listeners for {@link BlockChangeEvent} and
     *                            {@link InstanceUnregisterEvent} will be registered
     * @param executor            the {@link ExecutorService} used to run all pathfinding operations
     * @param instanceCache       the maximum size of the {@link PathCache} maintained for each instance
     * @param updateQueueCapacity the capacity of the update queue (maximum number of block state changes that will be
     *                            kept in-memory at any given time; larger values mean more memory is used but may
     *                            prevent unnecessary cache invalidation
     */
    public BasicContextProvider(@NotNull EventNode<Event> globalEventNode, @NotNull ExecutorService executor,
            int instanceCache, int updateQueueCapacity) {
        this.contextMap = new WeakHashMap<>();
        this.executorService = Objects.requireNonNull(executor, "executor");
        this.instanceCache = instanceCache;
        this.updateQueueCapacity = updateQueueCapacity;

        globalEventNode.addListener(BlockChangeEvent.class, this::onBlockChange);
    }

    @Override
    public @NotNull PathContext provideContext(@NotNull Instance instance) {
        return contextMap.computeIfAbsent(instance,
                newInstance -> new BasicPathContext(new BasicPathEngine(executorService),
                        new SpatialCollider(new InstanceSpace(newInstance)),
                        new BasicPathCache(instanceCache, updateQueueCapacity)));
    }

    private void onBlockChange(@NotNull BlockChangeEvent event) {
        PathContext context = contextMap.get(event.getInstance());
        if (context != null) {
            context.getCache().handleUpdate(VecUtils.toBlockInt(event.blockPosition()),
                    SolidProvider.fromShape(event.getOldBlock().registry().collisionShape()),
                    SolidProvider.fromShape(event.getBlock().registry().collisionShape()));
        }
    }
}