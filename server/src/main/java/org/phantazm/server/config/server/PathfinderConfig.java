package org.phantazm.server.config.server;

import java.util.concurrent.TimeUnit;

/**
 * Config for pathfinding.
 */
public record PathfinderConfig(
    int threads,
    boolean asyncMode,
    int corePoolSize,
    int maximumPoolSize,
    int minimumRunnable,
    long keepAliveTime,
    TimeUnit keepAliveTimeUnit) {
    /**
     * The default PathfinderConfig.
     */
    public static final PathfinderConfig DEFAULT;

    static {
        int threads = Runtime.getRuntime().availableProcessors();
        DEFAULT = new PathfinderConfig(threads, false, threads, threads, threads, 2, TimeUnit.MINUTES);
    }
}
