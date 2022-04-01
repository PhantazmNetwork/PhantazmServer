package com.github.phantazmnetwork.neuron.engine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.phantazmnetwork.commons.iterator.AdvancingIterator;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * The standard implementation of {@link PathContext}. Agents with identical descriptor IDs will share cached values.
 * Uses a global cache of {@link Iterable}s to avoid duplication (as it is expected that many iterables will be
 * identical across all contexts).
 */
public class BasicPathContext implements PathContext {
    private static final int INITIAL_HASHMAP_CAPACITY = 8;

    private final Cache<Vec3I, Map<String, Iterable<Vec3I>>> positionalCache;

    //global cache shared among all BasicPathContext instances. reduces duplication of cached Iterables (lists)
    private static final Cache<List<Vec3I>, Iterable<Vec3I>> ITERABLE_CACHE = Caffeine.newBuilder().maximumSize(128)
            .build();

    /**
     * Creates a new instance of this class with the specified maximum cache size. The number of cached iterables will
     * never exceed this value for very long. Old values will be evicted according to an LRU algorithm.
     * @param maximumCacheSize the maximum cache size
     * @see Cache
     */
    public BasicPathContext(int maximumCacheSize) {
        this.positionalCache = Caffeine.newBuilder().maximumSize(maximumCacheSize).build();
    }

    @Override
    public @NotNull Optional<Iterable<Vec3I>> getStep(@NotNull Vec3I origin, @NotNull String id) {
        Map<String, Iterable<Vec3I>> entries = positionalCache.getIfPresent(origin);
        if(entries != null) {
            return Optional.ofNullable(entries.get(id));
        }

        return Optional.empty();
    }

    @Override
    public @NotNull Iterator<Vec3I> watchSteps(@NotNull Vec3I origin, @NotNull String id,
                                               @NotNull Iterator<? extends Vec3I> steps) {
        return new AdvancingIterator<>() {
            private final List<Vec3I> list = new ArrayList<>();
            private boolean complete = false;

            @Override
            public boolean advance() {
                if(complete) {
                    return false;
                }

                if(steps.hasNext()) {
                    list.add(this.value = steps.next());
                    return true;
                }

                positionalCache.get(origin, ignored -> Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>(
                        INITIAL_HASHMAP_CAPACITY))).compute(id, (key, value) -> getView(list));
                complete = true;
                return false;
            }
        };
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

    //List is specified here instead of Collection because the latter's contract does not mandate an equals/hashCode
    //based on contents
    private Iterable<Vec3I> getView(List<Vec3I> list) {
        if(list.size() == 0) {
            return Collections.emptyList();
        }

        return ITERABLE_CACHE.get(list, Collections::unmodifiableList);
    }
}