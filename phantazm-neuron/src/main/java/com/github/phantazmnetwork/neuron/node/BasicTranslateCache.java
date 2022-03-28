package com.github.phantazmnetwork.neuron.node;

import com.github.phantazmnetwork.commons.sync.LockUtils;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BasicTranslateCache implements TranslateCache {
    private record TranslateKey(Vec3I getPosition, Vec3I getOffset) {}

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final SortedMap<Agent, Map<TranslateKey, Vec3I>> cache = new Object2ObjectAVLTreeMap<>();

    @Override
    public @NotNull Result forAgent(@NotNull Agent agent, int x, int y, int z, int dX, int dY, int dZ) {
        SortedMap<Agent, Map<TranslateKey, Vec3I>> tail = cache.tailMap(agent);
        return LockUtils.lock(readWriteLock.readLock(), () -> {
            if(!tail.isEmpty()) {
                //pick the smallest agent as it can go all places larger agents can
                Agent smallest = tail.firstKey();
                Map<TranslateKey, Vec3I> map = cache.get(smallest);
                if(!map.isEmpty()) {
                    TranslateKey key = new TranslateKey(Vec3I.of(x, y, z), Vec3I.of(dX, dY, dZ));
                    Vec3I cached = map.get(key);

                    if(cached != null) {
                        //cache hit
                        return new Result(true, cached);
                    }
                    else if(agent.compareTo(smallest) == 0 && map.containsKey(key)) {
                        /*
                        null is a cache hit if and only if the smallest agent is the same size as us. if we're smaller
                        than the smallest agent currently in the cache, we might be able to go places it can't, so it's
                        necessary to indicate a cache miss
                         */
                        return Result.NULL_HIT;
                    }
                }
            }

            return Result.MISS;
        });
    }

    @Override
    public void offer(@NotNull Agent agent, int x, int y, int z, int dX, int dY, int dZ, @Nullable Vec3I result) {
        //get agents smaller than this (they can go all places we can)
        SortedMap<Agent, Map<TranslateKey, Vec3I>> head = cache.headMap(agent);
        LockUtils.lock(readWriteLock.readLock(), () -> {
            if(result != null) {
                Agent agentKey;

                //if head is empty, we may have an exact match for agent, or a smaller element
                if(!head.isEmpty()) {
                    //if we have smaller elements, pick the smallest
                    agentKey = head.firstKey();
                }
                else {
                    agentKey = agent;
                }

                LockUtils.lock(readWriteLock.writeLock(), () -> {
                    cache.computeIfAbsent(agentKey, BasicTranslateCache::makeMap).put(new TranslateKey(Vec3I.of(x, y,
                            z), Vec3I.of(dX, dY, dZ)), result);
                });
            }
            else {
                //null result means we only store the value if we have an identical agent
                Map<TranslateKey, Vec3I> map = cache.get(agent);
                if(map != null) {
                    LockUtils.lock(readWriteLock.writeLock(), () -> {
                        map.put(new TranslateKey(Vec3I.of(x, y, z), Vec3I.of(dX, dY, dZ)), null);
                    });
                }
            }
        });
    }

    @Override
    public void remove(@NotNull Agent agent) {
        LockUtils.lock(readWriteLock.writeLock(), () -> {
            cache.remove(agent);
        });
    }

    @Override
    public void clear() {
        LockUtils.lock(readWriteLock.writeLock(), cache::clear);
    }

    private static Map<TranslateKey, Vec3I> makeMap(Agent agent) {
        return new Object2ObjectOpenHashMap<>();
    }
}
