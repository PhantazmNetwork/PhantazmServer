package com.github.phantazmnetwork.neuron.engine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.phantazmnetwork.commons.pipe.Pipe;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * The standard implementation of {@link PathCache}. Agents with identical descriptor IDs will share cached values.
 * Uses a global cache of {@link Iterable}s to avoid duplication (as it is expected that many iterables will be
 * identical across caches).
 */
public class BasicPathCache implements PathCache {
    private static final int INITIAL_HASHMAP_CAPACITY = 8;
    private static final int CACHE_EXPIRE_SECONDS = 5;

    private final Cache<Vec3I, Map<String, Iterable<Vec3I>>> positionalCache;

    //global cache shared among all BasicPathContext instances. reduces duplication of cached Iterables (lists)
    private static final Cache<ArrayList<Vec3I>, Iterable<Vec3I>> ITERABLE_CACHE = Caffeine.newBuilder()
            .maximumSize(128).build();

    /**
     * Creates a new instance of this class with the specified maximum cache size. The number of cached iterables will
     * never exceed this value for very long. Old values will be evicted according to an LRU algorithm.
     * @param maximumCacheSize the maximum cache size
     * @see Cache
     */
    public BasicPathCache(int maximumCacheSize) {
        this.positionalCache = Caffeine.newBuilder().maximumSize(maximumCacheSize).expireAfterWrite(
                CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS).build();
    }

    @Override
    public @NotNull Optional<Iterable<Vec3I>> getSteps(@NotNull Vec3I origin, @NotNull String id) {
        Map<String, Iterable<Vec3I>> entries = positionalCache.getIfPresent(origin);
        if(entries != null) {
            return Optional.ofNullable(entries.get(id));
        }

        return Optional.empty();
    }

    @Override
    public @NotNull Iterator<Vec3I> watchSteps(@NotNull Vec3I origin, @NotNull String id,
                                               @NotNull Iterator<Vec3I> steps) {
        ArrayList<Vec3I> list = new ArrayList<>();
        return Pipe.from(steps).listen(list::add).whenLast(ignored -> positionalCache.get(origin, ignored2 ->
                Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>(INITIAL_HASHMAP_CAPACITY))).put(id,
                getView(list)));
    }

    @Override
    public void invalidateOrigin(@NotNull Vec3I origin) {
        positionalCache.invalidate(origin);
    }

    @Override
    public void invalidateOrigins(@NotNull Iterable<? extends Vec3I> steps) {
        positionalCache.invalidateAll(steps);
    }

    @Override
    public void invalidateAll() {
        positionalCache.invalidateAll();
    }

    //ArrayList is specified here instead of Collection because the latter's contract does not mandate an
    //equals/hashCode based on contents + we may want to use trimToSize() to ensure we don't waste space
    private Iterable<Vec3I> getView(ArrayList<Vec3I> list) {
        if(list.isEmpty()) {
            return Collections.emptyList();
        }

        //only trim the list if it's getting stored, otherwise it'll just be garbage collected
        return ITERABLE_CACHE.get(list, key -> {
            key.trimToSize();
            return Collections.unmodifiableList(key);
        });
    }
}