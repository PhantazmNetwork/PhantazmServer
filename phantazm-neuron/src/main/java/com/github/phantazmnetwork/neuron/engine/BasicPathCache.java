package com.github.phantazmnetwork.neuron.engine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.phantazmnetwork.commons.pipe.Pipe;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Descriptor;
import com.github.phantazmnetwork.neuron.world.Solid;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * The standard implementation of {@link PathCache}. Agents with identical descriptor IDs will share cached values.
 * Uses a global cache of {@link Iterable}s to avoid duplication (as it is expected that many iterables will be
 * identical across caches).
 */
public class BasicPathCache implements PathCache {
    private static final int INITIAL_HASHMAP_CAPACITY = 8;
    private static final int ITERABLE_CACHE_SIZE = 128;
    //global cache shared among all BasicPathContext instances. reduces duplication of cached Iterables (lists)
    private static final Cache<ArrayList<Vec3I>, Iterable<Vec3I>> ITERABLE_CACHE =
            Caffeine.newBuilder().maximumSize(ITERABLE_CACHE_SIZE).build();
    private final Deque<Update> updateQueue;
    private final Cache<Vec3I, Map<String, CacheEntry>> positionalCache;
    private final int updateQueueCapacity;
    private volatile long update;

    /**
     * Creates a new instance of this class with the specified maximum cache size. The number of cached iterables will
     * never exceed this value for very long. Old values will be evicted according to an LRU algorithm.
     *
     * @param maximumCacheSize    the maximum cache size
     * @param updateQueueCapacity the maximum size of the update queue
     * @see Cache
     */
    public BasicPathCache(int maximumCacheSize, int updateQueueCapacity) {
        this.updateQueue = new ArrayDeque<>();
        this.positionalCache = Caffeine.newBuilder().maximumSize(maximumCacheSize).build();
        this.updateQueueCapacity = updateQueueCapacity;
    }

    @Override
    public @NotNull Optional<Iterable<Vec3I>> getSteps(@NotNull Vec3I origin, @NotNull Descriptor descriptor) {
        Map<String, CacheEntry> entries = positionalCache.getIfPresent(origin);
        if (entries != null) {
            CacheEntry entry = entries.get(descriptor.getID());
            if (entry != null) {
                synchronized (updateQueue) {
                    long mostRecent = update;
                    long diff = mostRecent - entry.update;
                    entry.update = mostRecent;

                    if (diff == 0) {
                        //we're up-to-date, no blocks to check against
                        return Optional.of(entry.steps);
                    }

                    //for sufficiently out of date entries, don't bother iterating, consider invalid
                    //also allows us to evict sufficiently old updates in handleUpdate, since they won't be used
                    if (diff > updateQueueCapacity) {
                        positionalCache.invalidate(origin);
                        return Optional.empty();
                    }

                    Iterator<Update> iterator = updateQueue.descendingIterator();
                    Update update;
                    while (iterator.hasNext() && (update = iterator.next()).id > mostRecent) {
                        if (descriptor.shouldInvalidate(entry.steps, origin, update.position, update.oldSolid,
                                                        update.newSolid
                        )) {
                            positionalCache.invalidate(origin);
                            return Optional.empty();
                        }
                    }

                    return Optional.of(entry.steps);
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public @NotNull Iterator<Vec3I> watchSteps(@NotNull Vec3I origin, @NotNull Descriptor descriptor,
                                               @NotNull Iterator<Vec3I> steps) {
        long update;
        synchronized (updateQueue) {
            update = this.update;
        }

        ArrayList<Vec3I> list = new ArrayList<>(8);
        return Pipe.from(steps).listen(list::add).forLast(ignored -> positionalCache.get(origin,
                                                                                         ignored2 -> Object2ObjectMaps.synchronize(
                                                                                                 new Object2ObjectOpenHashMap<>(
                                                                                                         INITIAL_HASHMAP_CAPACITY))
        ).put(descriptor.getID(), new CacheEntry(getView(list), update)));
    }

    @Override
    public void handleUpdate(@NotNull Vec3I position, @NotNull Solid oldSolid, @NotNull Solid newSolid) {
        synchronized (updateQueue) {
            updateQueue.add(new Update(position, ++update, oldSolid, newSolid));

            if (updateQueue.size() > updateQueueCapacity) {
                //first entry is stale, cache entries that reference it will always be invalidated
                updateQueue.removeFirst();
            }
        }
    }

    //ArrayList is specified here instead of Collection because the latter's contract does not mandate an
    //equals/hashCode based on contents + we may want to use trimToSize() to ensure we don't waste space
    private Iterable<Vec3I> getView(ArrayList<Vec3I> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        //only trim the list if it's getting stored, otherwise it'll just be garbage collected
        return ITERABLE_CACHE.get(list, key -> {
            key.trimToSize();
            return Collections.unmodifiableList(key);
        });
    }

    private static class CacheEntry {
        private final Iterable<Vec3I> steps;
        private long update;

        private CacheEntry(Iterable<Vec3I> steps, long update) {
            this.steps = steps;
            this.update = update;
        }
    }

    private record Update(Vec3I position, long id, Solid oldSolid, Solid newSolid) {
    }
}
