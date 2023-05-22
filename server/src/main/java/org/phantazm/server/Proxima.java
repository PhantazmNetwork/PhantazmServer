package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.proxima.path.BasicAsyncPathfinder;
import com.github.steanky.proxima.path.BasicPathOperation;
import com.github.steanky.proxima.path.Pathfinder;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.proxima.bindings.minestom.*;
import org.phantazm.server.config.server.PathfinderConfig;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public final class Proxima {
    private static Pathfinder pathfinder;
    private static Function<? super Instance, ? extends InstanceSpawner.InstanceSettings> settingsFunction;
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
        settingsFunction = new InstanceSettingsFunction(globalNode);
        spawner = new InstanceSpawner(pathfinder, settingsFunction);
    }

    private static void registerElementClasses(@NotNull ContextManager contextManager) {
        contextManager.registerElementClass(GroundPathfindingFactory.class);
    }

    public static @NotNull Pathfinder getPathfinder() {
        return FeatureUtils.check(pathfinder);
    }

    public static @NotNull Function<? super Instance, ? extends InstanceSpawner.InstanceSettings> instanceSettingsFunction() {
        return FeatureUtils.check(settingsFunction);
    }

    public static @NotNull Spawner getSpawner() {
        return FeatureUtils.check(spawner);
    }
}
