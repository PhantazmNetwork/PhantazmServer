package com.github.phantazmnetwork.neuron.engine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.phantazmnetwork.commons.iterator.AdvancingIterator;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BasicPathContext implements PathContext {
    private static final class Entry implements Comparable<Entry> {
        private final int descriptor;

        //mutable so we can update entries in-place instead of needing to remove from the set
        private Iterable<Vec3I> iterable;

        private Entry(int descriptor, Iterable<Vec3I> iterable) {
            this.descriptor = descriptor;
            this.iterable = iterable;
        }

        @Override
        public int compareTo(@NotNull BasicPathContext.Entry o) {
            return Integer.compare(descriptor, o.descriptor);
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
            return Integer.hashCode(descriptor);
        }
    }

    //shared cache for immutable collections: should help cut down on useless duplicate objects
    private static final Cache<Collection<? extends Vec3I>, Iterable<Vec3I>> ITERABLE_CACHE = Caffeine.newBuilder()
            .maximumSize(256).build();

    private final IntSet descriptors;
    private final Cache<Vec3I, SortedSet<Entry>> cache;

    public BasicPathContext(@NotNull IntSet descriptors) {
        this.cache = Caffeine.newBuilder().maximumSize(1024).build();
        this.descriptors = new IntOpenHashSet(descriptors);
    }

    @Override
    public @Nullable Iterable<Vec3I> getStep(int descriptor, @NotNull Vec3I origin) {
        SortedSet<Entry> entries = cache.getIfPresent(origin);
        if(entries != null) {
            SortedSet<Entry> largerOrEqual = entries.tailSet(new Entry(descriptor, null));
            if(!largerOrEqual.isEmpty()) {
                return largerOrEqual.first().iterable;
            }
        }

        return null;
    }

    @Override
    public @NotNull Iterator<Vec3I> watchSteps(int descriptor, @NotNull Vec3I origin,
                                               @NotNull Iterator<? extends Vec3I> steps) {
        return new AdvancingIterator<>() {
            private final ArrayList<Vec3I> list = new ArrayList<>();

            @Override
            public boolean advance() {
                if(steps.hasNext()) {
                    list.add(this.value = steps.next());
                    return true;
                }
                else {
                    SortedSet<Entry> entries = cache.get(origin, key -> new ObjectRBTreeSet<>());
                    Entry key = new Entry(descriptor, null);

                    if(entries.contains(key)) {
                        SortedSet<Entry> entriesTail = entries.tailSet(key);
                        entriesTail.first().iterable = makeView(list);
                    }
                    else {
                        SortedSet<Entry> entriesHead = entries.headSet(key);
                        if(!entriesHead.isEmpty()) {
                            entriesHead.first().iterable = makeView(list);
                        }
                        else if(descriptors.contains(descriptor)) {
                            //only add new entry if it's in descriptors
                            key.iterable = makeView(list);
                            entries.add(key);
                        }
                    }

                    return false;
                }
            }
        };
    }

    @Override
    public void invalidateOrigin(@NotNull Vec3I origin) {
        cache.invalidate(origin);
    }

    @Override
    public void invalidateOrigins(@NotNull Iterable<? extends Vec3I> steps) {
        cache.invalidateAll(steps);
    }

    @Override
    public void invalidateAll() {
        cache.invalidateAll();
    }

    private Iterable<Vec3I> makeView(ArrayList<? extends Vec3I> arrayList) {
        if(arrayList.size() == 0) {
            return Collections.emptyList();
        }

        return ITERABLE_CACHE.get(arrayList, key -> Collections.unmodifiableCollection(arrayList));
    }
}
