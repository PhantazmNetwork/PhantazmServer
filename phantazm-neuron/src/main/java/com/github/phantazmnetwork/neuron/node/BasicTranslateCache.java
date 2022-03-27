package com.github.phantazmnetwork.neuron.node;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BasicTranslateCache implements TranslateCache {
    private static class PositionCacheKey implements Comparable<AgentCacheKey> {
        protected final Vec3I position;
        protected final Vec3I delta;

        private PositionCacheKey(Vec3I position, Vec3I delta) {
            this.position = position;
            this.delta = delta;
        }

        @Override
        public int compareTo(@NotNull BasicTranslateCache.AgentCacheKey o) {
            int positionCompare = position.compareTo(o.position);
            if(positionCompare == 0) {
                return delta.compareTo(o.delta);
            }

            return positionCompare;
        }
    }

    private static class AgentCacheKey extends PositionCacheKey {
        private final Agent agent;

        private AgentCacheKey(Agent agent, Vec3I position, Vec3I delta) {
            super(position, delta);
            this.agent = agent;
        }

        @Override
        public int compareTo(@NotNull BasicTranslateCache.AgentCacheKey o) {
            int agentCompare = agent.compareTo(o.agent);
            if(agentCompare == 0) {
                return super.compareTo(o);
            }

            return agentCompare;
        }
    }

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final SortedMap<PositionCacheKey, Vec3I> cache = new Object2ObjectAVLTreeMap<>();

    @Override
    public @NotNull Result forAgent(@NotNull Agent agent, int x, int y, int z, int dX, int dY, int dZ) {
        Vec3I position = Vec3I.of(x, y, z);
        Vec3I delta = Vec3I.of(dX, dY, dZ);

        AgentCacheKey agentKey = new AgentCacheKey(agent, position, delta);

        Lock readLock = readWriteLock.readLock();
        try {
            readLock.lock();
            SortedMap<PositionCacheKey, Vec3I> candidates = cache.tailMap(agentKey);

            //no smaller agents, but we might have an exact match
            if(candidates.isEmpty()) {
                //no exact match: cache miss
                if(!cache.containsKey(agentKey)) {
                    return Result.MISS;
                }

                Vec3I translate = cache.get(agentKey);
                return translate == null ? Result.NULL_HIT : new Result(true, translate);
            }

            //now, search candidates using ONLY the position as a key, which ignores the agent
            PositionCacheKey positionKey = new PositionCacheKey(position, delta);
            if(!candidates.containsKey(positionKey)) {
                return Result.MISS;
            }

            Vec3I translate = candidates.get(positionKey);
            return translate == null ? Result.NULL_HIT : new Result(true, translate);
        }
        finally {
            readLock.unlock();
        }
    }

    @Override
    public void offer(@NotNull Agent agent, int x, int y, int z, int dX, int dY, int dZ, @Nullable Vec3I result) {
        Lock writeLock = readWriteLock.writeLock();

        AgentCacheKey key = new AgentCacheKey(agent, Vec3I.of(x, y, z), Vec3I.of(dX, dY, dZ));

        try {
            writeLock.lock();
            cache.put(key, result);
        }
        finally {
            writeLock.unlock();
        }
    }

    @Override
    public void remove(@NotNull Agent agent) {
        Iterator<Map.Entry<PositionCacheKey, Vec3I>> entryIterator = cache.entrySet().iterator();
        Lock writeLock = readWriteLock.writeLock();

        try {
            writeLock.lock();
            while(entryIterator.hasNext()) {
                Map.Entry<PositionCacheKey, Vec3I> entry = entryIterator.next();
                if(entry.getKey() instanceof AgentCacheKey agentKey && agentKey.agent == agent) {
                    entryIterator.remove();
                }
            }
        }
        finally {
            writeLock.unlock();
        }
    }

    @Override
    public void clear() {
        Lock writeLock = readWriteLock.writeLock();
        try {
            writeLock.lock();
            cache.clear();
        }
        finally {
            writeLock.unlock();
        }
    }

    @Override
    public int size() {
        Lock readLock = readWriteLock.readLock();
        try {
            readLock.lock();
            return cache.size();
        }
        finally {
            readLock.unlock();
        }
    }
}
