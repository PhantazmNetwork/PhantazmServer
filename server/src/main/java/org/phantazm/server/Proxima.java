package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.proxima.path.BasicAsyncPathfinder;
import com.github.steanky.proxima.path.BasicPathOperation;
import com.github.steanky.proxima.path.Pathfinder;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;
import org.phantazm.proxima.bindings.minestom.GroundPathfindingFactory;
import org.phantazm.proxima.bindings.minestom.InstanceSettingsFunction;
import org.phantazm.proxima.bindings.minestom.InstanceSpawner;
import org.phantazm.proxima.bindings.minestom.Spawner;
import org.phantazm.proxima.bindings.minestom.goal.GoalGroup;
import org.phantazm.server.config.server.PathfinderConfig;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public final class Proxima {
    private static Pathfinder pathfinder;
    private static Spawner spawner;

    private Proxima() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull EventNode<Event> globalNode, @NotNull ContextManager contextManager,
            @NotNull PathfinderConfig pathfinderConfig) {
        registerElementClasses(contextManager);

        int threads = pathfinderConfig.threads();
        boolean asyncMode = pathfinderConfig.asyncMode();
        int corePoolSize = pathfinderConfig.corePoolSize();
        int maximumPoolSize = pathfinderConfig.maximumPoolSize();
        int minimumRunnable = pathfinderConfig.minimumRunnable();
        long keepAliveTime = pathfinderConfig.keepAliveTime();
        TimeUnit keepAliveTimeUnit = pathfinderConfig.keepAliveTimeUnit();

        ForkJoinPool fjp = new ForkJoinPool(threads, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, asyncMode,
                corePoolSize, maximumPoolSize, minimumRunnable, forkJoinPool -> true, keepAliveTime, keepAliveTimeUnit);

        pathfinder = new BasicAsyncPathfinder(fjp, BasicPathOperation::new, 1000000);
        spawner = new InstanceSpawner(pathfinder, new InstanceSettingsFunction(globalNode));
    }

    private static void registerElementClasses(@NotNull ContextManager contextManager) {
        contextManager.registerElementClass(GoalGroup.class);
        contextManager.registerElementClass(GroundPathfindingFactory.class);
    }

    public static @NotNull Pathfinder getPathfinder() {
        return FeatureUtils.check(pathfinder);
    }

    public static @NotNull Spawner getSpawner() {
        return FeatureUtils.check(spawner);
    }
}
