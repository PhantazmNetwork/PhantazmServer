package com.github.phantazmnetwork.neuron.node;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.phantazmnetwork.commons.logic.Wrapper;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.SortedSet;

public class BasicTranslateCache implements TranslateCache {
    private record CacheKey(Agent.Descriptor getDescriptor, Vec3I getPosition, Vec3I getOffset) { }

    //descriptors set is only written to during object construction, therefore we don't need to synchronize reads
    private final SortedSet<Agent.Descriptor> descriptors = new ObjectRBTreeSet<>();

    //cache impls are specified to be threadsafe
    private final Cache<CacheKey, Wrapper<Vec3I>> cache;

    public BasicTranslateCache(@NotNull Iterable<? extends Agent.Descriptor> descriptors) {
        for(Agent.Descriptor descriptor : descriptors) {
            if(!this.descriptors.add(descriptor)) {
                throw new IllegalArgumentException("Duplicate descriptors are not permitted");
            }
        }

        if(this.descriptors.isEmpty()) {
            throw new IllegalArgumentException("Iterable must supply at least one descriptor");
        }

        this.cache = Caffeine.newBuilder().maximumSize(2048).build();
    }

    @Override
    public @NotNull Result forAgent(@NotNull Agent.Descriptor descriptor, int x, int y, int z, int dX, int dY, int dZ) {
        SortedSet<Agent.Descriptor> tail = descriptors.tailSet(descriptor);

        //operations on cache don't need to be synchronized because it is threadsafe
        if(!tail.isEmpty()) {
            Wrapper<Vec3I> vectorWrapper = cache.getIfPresent(new CacheKey(tail.first(), Vec3I.of(x, y, z), Vec3I
                    .of(dX, dY, dZ)));
            if(vectorWrapper != null) {
                Vec3I value = vectorWrapper.get();
                if(value != null) {
                    return new Result(true, value);
                }
                else if(descriptors.contains(descriptor)) {
                    return Result.NULL_HIT;
                }
            }
        }

        return Result.MISS;
    }

    @Override
    public void offer(@NotNull Agent.Descriptor descriptor, int x, int y, int z, int dX, int dY, int dZ,
                      @Nullable Vec3I result) {
        SortedSet<Agent.Descriptor> head = descriptors.headSet(descriptor);

        //null results are treated differently: only cache if we're an exact match
        if(result == null) {
            if(descriptors.contains(descriptor)) {
                cache.put(new CacheKey(descriptor, Vec3I.of(x, y, z), Vec3I.of(dX, dY, dZ)), Wrapper.nullWrapper());
            }
        }
        else if(!head.isEmpty()) {
            cache.put(new CacheKey(head.first(), Vec3I.of(x, y, z), Vec3I.of(dX, dY, dZ)), Wrapper.immutable(result));
        }
    }

    @Override
    public void clear() {
        cache.invalidateAll();
        cache.cleanUp();
    }
}