package com.github.phantazmnetwork.neuron.engine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.phantazmnetwork.commons.iterator.AdvancingIterator;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
public class BasicPathContext implements PathContext {
    private final class Entry implements Comparable<Entry> {
        private final Agent.Descriptor descriptor;

        //mutable so we can update entries in-place instead of needing to remove from the set
        private Iterable<Vec3I> iterable;

        private Entry(Agent.Descriptor descriptor, Iterable<Vec3I> iterable) {
            this.descriptor = descriptor;
            this.iterable = iterable;
        }

        private Entry(Agent.Descriptor descriptor) {
            this(descriptor, null);
        }

        @Override
        public int compareTo(@NotNull Entry other) {
            return comparator.compare(descriptor, other.descriptor);
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == null) {
                return false;
            }

            if(obj instanceof Entry entry) {
                return descriptor == entry.descriptor;
            }

            return false;
        }

        @Override
        public int hashCode() {
            return descriptor.hashCode();
        }
    }

    private final Comparator<Agent.Descriptor> comparator;
    private final Cache<Vec3I, ObjectOpenHashSet<Entry>> positionalCache;

    //global cache shared among all BasicPathContext instances. reduces duplication of cached Iterables (lists)
    private static final Cache<List<Vec3I>, Iterable<Vec3I>> ITERABLE_CACHE = Caffeine.newBuilder().maximumSize(128)
            .build();

    public BasicPathContext(@NotNull Comparator<Agent.Descriptor> comparator, int maximumCacheSize) {
        this.comparator = Objects.requireNonNull(comparator, "comparator");
        this.positionalCache = Caffeine.newBuilder().maximumSize(maximumCacheSize).build();
    }

    @Override
    public @NotNull Optional<Iterable<Vec3I>> getStep(@NotNull Vec3I origin, @NotNull Agent.Descriptor descriptor) {
        ObjectOpenHashSet<Entry> entries = positionalCache.getIfPresent(origin);
        if(entries != null) {
            //use our own synchronized block instead of wrapping cache entries in a synchronized collection: if we did
            //the latter, we'd not be able to take advantage of the get method, which is defined on ObjectRBTreeSet
            Entry hit;
            synchronized (entries) {
                hit = entries.get(new Entry(descriptor));
            }

            if(hit != null) {
                //.of instead of .ofNullable because the iterable should never be null under normal conditions, and if
                //it is, it's probably a bug
                return Optional.of(hit.iterable);
            }
        }

        return Optional.empty();
    }

    @Override
    public @NotNull Iterator<Vec3I> watchSteps(@NotNull Vec3I origin, @NotNull Agent.Descriptor descriptor,
                                               @NotNull Iterator<? extends Vec3I> steps) {
        return new AdvancingIterator<>() {
            private final List<Vec3I> list = new ArrayList<>();

            @Override
            public boolean advance() {
                if(steps.hasNext()) {
                    list.add(this.value = steps.next());
                    return true;
                }

                ObjectOpenHashSet<Entry> entries = positionalCache.get(origin, key -> new ObjectOpenHashSet<>());
                Entry inSet;
                synchronized (entries) {
                    //this method is specific to ObjectOpenHashSet and highly useful here, so we can avoid removing and
                    //adding and instead update the entry in-place
                    inSet = entries.addOrGet(new Entry(descriptor));
                }

                inSet.iterable = getView(list);
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
