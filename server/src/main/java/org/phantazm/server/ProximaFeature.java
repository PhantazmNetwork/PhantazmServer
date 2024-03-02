package org.phantazm.server;

import com.github.steanky.proxima.path.BasicAsyncPathfinder;
import com.github.steanky.proxima.path.BasicPathOperation;
import com.github.steanky.proxima.path.Pathfinder;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.proxima.bindings.minestom.InstanceSettingsFunction;
import org.phantazm.proxima.bindings.minestom.InstanceSpawner;
import org.phantazm.server.config.server.PathfinderConfig;
import org.phantazm.server.context.ConfigContext;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public final class ProximaFeature {
    private static Pathfinder pathfinder;
    private static Function<? super Instance, ? extends InstanceSpawner.InstanceSettings> settingsFunction;

    private ProximaFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull ConfigContext configContext) {
        PathfinderConfig pathfinderConfig = configContext.pathfinderConfig();

        int threads = pathfinderConfig.threads();
        boolean asyncMode = pathfinderConfig.asyncMode();
        int corePoolSize = pathfinderConfig.corePoolSize();
        int maximumPoolSize = pathfinderConfig.maximumPoolSize();
        int minimumRunnable = pathfinderConfig.minimumRunnable();
        long keepAliveTime = pathfinderConfig.keepAliveTime();
        TimeUnit keepAliveTimeUnit = pathfinderConfig.keepAliveTimeUnit();

        ForkJoinPool fjp = new ForkJoinPool(threads, new ForkJoinPool.ForkJoinWorkerThreadFactory() {
            private final AtomicInteger count = new AtomicInteger();

            private static class ForkJoinWorkerThreadImpl extends ForkJoinWorkerThread {
                protected ForkJoinWorkerThreadImpl(ForkJoinPool pool) {
                    super(pool);
                }
            }

            @Override
            public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
                ForkJoinWorkerThread thread = new ForkJoinWorkerThreadImpl(pool);
                thread.setName("proxima-worker-" + count.getAndIncrement());
                return thread;
            }
        }, null, asyncMode,
            corePoolSize, maximumPoolSize, minimumRunnable, forkJoinPool -> true, keepAliveTime, keepAliveTimeUnit);

        pathfinder = new BasicAsyncPathfinder(fjp, BasicPathOperation::new, 1000000);
        settingsFunction = new InstanceSettingsFunction(MinecraftServer.getGlobalEventHandler());
    }

    public static @NotNull Pathfinder getPathfinder() {
        return FeatureUtils.check(pathfinder);
    }

    public static @NotNull Function<? super Instance, ? extends InstanceSpawner.InstanceSettings> instanceSettingsFunction() {
        return FeatureUtils.check(settingsFunction);
    }
}
