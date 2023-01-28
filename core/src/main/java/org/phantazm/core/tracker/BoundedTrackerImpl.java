package org.phantazm.core.tracker;

import com.github.steanky.vector.Bounds3I;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.HashStrategies;

import java.util.*;
import java.util.function.Consumer;

class BoundedTrackerImpl<T extends Bounded> implements BoundedTracker<T> {
    private final List<T> items;
    private final Long2ObjectMap<T[]> chunkedItems;

    BoundedTrackerImpl(@NotNull Collection<T> items) {
        this.items = List.copyOf(items);
        this.chunkedItems = chunkItems(items);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Bounded> Long2ObjectMap<T[]> chunkItems(Collection<T> items) {
        Long2ObjectOpenHashMap<Set<T>> map = new Long2ObjectOpenHashMap<>();

        for (T item : items) {
            List<Bounds3I> allBounds = item.bounds();

            for (Bounds3I bounds : allBounds) {
                int startX = bounds.originX() >> 4;
                int startZ = bounds.originZ() >> 4;

                int endX = Math.max(0, bounds.maxX() - 1) >> 4;
                int endZ = Math.max(0, bounds.maxZ() - 1) >> 4;

                for (int cx = startX; cx <= endX; cx++) {
                    for (int cz = startZ; cz <= endZ; cz++) {
                        map.computeIfAbsent(ChunkUtils.getChunkIndex(cx, cz),
                                ignored -> new ObjectOpenCustomHashSet<>(4, HashStrategies.identity())).add(item);
                    }
                }
            }
        }

        Long2ObjectOpenHashMap<T[]> arrayMap = new Long2ObjectOpenHashMap<>(map.size());
        for (Long2ObjectMap.Entry<Set<T>> entry : map.long2ObjectEntrySet()) {
            arrayMap.put(entry.getLongKey(), (T[])entry.getValue().toArray(Object[]::new));
        }

        return arrayMap;
    }

    @Override
    public @NotNull Optional<T> closestInRange(@NotNull Point origin, double distance) {
        int startX = (int)Math.floor(origin.x() - distance) >> 4;
        int startZ = (int)Math.floor(origin.z() - distance) >> 4;

        int endX = (int)Math.floor(origin.x() + distance - Vec.EPSILON) >> 4;
        int endZ = (int)Math.floor(origin.z() + distance - Vec.EPSILON) >> 4;

        T closest = null;
        double closestDistance = Double.POSITIVE_INFINITY;

        for (int cx = startX; cx <= endX; cx++) {
            for (int cz = startZ; cz <= endZ; cz++) {
                T[] chunkItems = chunkedItems.get(ChunkUtils.getChunkIndex(cx, cz));
                if (chunkItems != null) {
                    for (T item : chunkItems) {
                        double thisDistance = origin.distanceSquared(item.center());
                        if (thisDistance < closestDistance && thisDistance <= distance * distance) {
                            closest = item;
                            closestDistance = thisDistance;
                        }
                    }
                }
            }
        }

        return Optional.ofNullable(closest);
    }

    @Override
    public void forEachInRange(@NotNull Point origin, double distance, @NotNull Consumer<? super T> consumer) {
        int startX = (int)Math.floor(origin.x() - distance) >> 4;
        int startZ = (int)Math.floor(origin.z() - distance) >> 4;

        int endX = (int)Math.floor(origin.x() + distance - Vec.EPSILON) >> 4;
        int endZ = (int)Math.floor(origin.z() + distance - Vec.EPSILON) >> 4;

        for (int cx = startX; cx <= endX; cx++) {
            for (int cz = startZ; cz <= endZ; cz++) {
                T[] chunkItems = chunkedItems.get(ChunkUtils.getChunkIndex(cx, cz));
                if (chunkItems != null) {
                    for (T item : chunkItems) {
                        double thisDistance = origin.distanceSquared(item.center());
                        if (thisDistance <= distance * distance) {
                            consumer.accept(item);
                        }
                    }
                }
            }
        }
    }

    @Override
    public @NotNull Optional<T> atPoint(@NotNull Point point) {
        T[] chunkItems = chunkedItems.get(ChunkUtils.getChunkIndex(point.chunkX(), point.chunkZ()));
        if (chunkItems == null) {
            return Optional.empty();
        }

        for (T item : chunkItems) {
            for (Bounds3I bounds : item.bounds()) {
                if (bounds.contains(point.blockX(), point.blockY(), point.blockZ())) {
                    return Optional.of(item);
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public @NotNull @Unmodifiable List<T> items() {
        return items;
    }
}
