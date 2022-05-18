package com.github.phantazmnetwork.server.config.server;

/**
 * Config for pathfinding.
 */
public record PathfinderConfig(int threads, int cacheSize, int updateQueueCapacity) {
    /**
     * The default PathfinderConfig.
     */
    public static final PathfinderConfig DEFAULT = new PathfinderConfig(Runtime.getRuntime().availableProcessors(),
            1024, 1024);

    /**
     * Creates a config for pathfinding.
     * @param threads the number of threads to use per instance for pathfinding operations
     * @param cacheSize the size allocated to the cache, used to minimize collision checks
     */
    public PathfinderConfig {}
}
