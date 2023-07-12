package org.phantazm.core.tracker;

import com.github.steanky.vector.Bounds3I;
import com.github.steanky.vector.Vec3D;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.HashStrategies;
import org.phantazm.commons.MathUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
class BoundedTrackerImpl<T extends Bounded> implements BoundedTracker<T> {
    private final List<T> items;
    private final Long2ObjectMap<Object[]> chunkedItems;

    BoundedTrackerImpl(@NotNull Collection<T> items) {
        this.items = List.copyOf(items);
        this.chunkedItems = chunkItems(this.items);
    }

    private static <T extends Bounded> Long2ObjectMap<Object[]> chunkItems(Collection<T> items) {
        Long2ObjectOpenHashMap<Set<Object>> map = new Long2ObjectOpenHashMap<>();

        for (T item : items) {
            List<Bounds3I> allBounds = item.bounds();

            for (Bounds3I bounds : allBounds) {
                int startX = bounds.originX() >> 4;
                int startZ = bounds.originZ() >> 4;

                int endX = Math.max(bounds.originX(), bounds.maxX() - 1) >> 4;
                int endZ = Math.max(bounds.originZ(), bounds.maxZ() - 1) >> 4;

                for (int cx = startX; cx <= endX; cx++) {
                    for (int cz = startZ; cz <= endZ; cz++) {
                        map.computeIfAbsent(ChunkUtils.getChunkIndex(cx, cz),
                                ignored -> new ObjectOpenCustomHashSet<>(4, HashStrategies.identity())).add(item);
                    }
                }
            }
        }

        Long2ObjectOpenHashMap<Object[]> arrayMap = new Long2ObjectOpenHashMap<>(map.size());
        for (Long2ObjectMap.Entry<Set<Object>> entry : map.long2ObjectEntrySet()) {
            arrayMap.put(entry.getLongKey(), entry.getValue().toArray(Object[]::new));
        }

        return arrayMap;
    }

    @Override
    public @NotNull Optional<T> closestInRangeToBounds(@NotNull Point origin, double width, double height, double depth,
            double distance) {
        int startX = (int)Math.floor(origin.x() - (distance + (width / 2))) >> 4;
        int startZ = (int)Math.floor(origin.z() - (distance + (depth / 2))) >> 4;

        int endX = (int)Math.floor(origin.x() + (distance + (width / 2)) - Vec.EPSILON) >> 4;
        int endZ = (int)Math.floor(origin.z() + (distance + (depth / 2)) - Vec.EPSILON) >> 4;

        T closest = null;
        double closestDistance = Double.POSITIVE_INFINITY;

        double halfWidth = width / 2;
        double halfDepth = depth / 2;

        for (int cx = startX; cx <= endX; cx++) {
            for (int cz = startZ; cz <= endZ; cz++) {
                Object[] chunkItems = chunkedItems.get(ChunkUtils.getChunkIndex(cx, cz));
                if (chunkItems != null) {
                    for (Object item : chunkItems) {
                        T boundedItem = (T)item;

                        for (Bounds3I bounds : boundedItem.bounds()) {
                            double boundX = MathUtils.clamp(origin.x(), bounds.originX(), bounds.maxX());
                            double boundY = MathUtils.clamp(origin.y(), bounds.originY(), bounds.maxY());
                            double boundZ = MathUtils.clamp(origin.z(), bounds.originZ(), bounds.maxZ());

                            double agentX = MathUtils.clamp(boundX, origin.x() - halfWidth, origin.x() + halfWidth);
                            double agentY = MathUtils.clamp(boundY, origin.y(), origin.y() + height);
                            double agentZ = MathUtils.clamp(boundZ, origin.z() - halfDepth, origin.z() + halfDepth);

                            double thisDistance = Vec3D.distanceSquared(boundX, boundY, boundZ, agentX, agentY, agentZ);
                            if (thisDistance < distance * distance && thisDistance < closestDistance) {
                                closest = boundedItem;
                                closestDistance = thisDistance;
                            }
                        }
                    }
                }
            }
        }

        return Optional.ofNullable(closest);
    }

    @Override
    public @NotNull Optional<T> closestInRangeToBounds(@NotNull Point origin, double distance) {
        int startX = (int)Math.floor(origin.x() - distance) >> 4;
        int startZ = (int)Math.floor(origin.z() - distance) >> 4;

        int endX = (int)Math.floor(origin.x() + distance - Vec.EPSILON) >> 4;
        int endZ = (int)Math.floor(origin.z() + distance - Vec.EPSILON) >> 4;

        T closest = null;
        double closestDistance = Double.POSITIVE_INFINITY;

        for (int cx = startX; cx <= endX; cx++) {
            for (int cz = startZ; cz <= endZ; cz++) {
                Object[] chunkItems = chunkedItems.get(ChunkUtils.getChunkIndex(cx, cz));
                if (chunkItems != null) {
                    for (Object item : chunkItems) {
                        T boundedItem = (T)item;

                        for (Bounds3I bounds : boundedItem.bounds()) {
                            double boundX = MathUtils.clamp(origin.x(), bounds.originX(), bounds.maxX());
                            double boundY = MathUtils.clamp(origin.y(), bounds.originY(), bounds.maxY());
                            double boundZ = MathUtils.clamp(origin.z(), bounds.originZ(), bounds.maxZ());

                            double thisDistance =
                                    Vec3D.distanceSquared(boundX, boundY, boundZ, origin.x(), origin.y(), origin.z());
                            if (thisDistance < distance * distance && thisDistance < closestDistance) {
                                closest = boundedItem;
                                closestDistance = thisDistance;
                            }
                        }
                    }
                }
            }
        }

        return Optional.ofNullable(closest);
    }

    @Override
    public void forEachInRangeToCenter(@NotNull Point origin, double distance, @NotNull Consumer<? super T> consumer) {
        int startX = (int)Math.floor(origin.x() - distance) >> 4;
        int startZ = (int)Math.floor(origin.z() - distance) >> 4;

        int endX = (int)Math.floor(origin.x() + distance - Vec.EPSILON) >> 4;
        int endZ = (int)Math.floor(origin.z() + distance - Vec.EPSILON) >> 4;

        for (int cx = startX; cx <= endX; cx++) {
            for (int cz = startZ; cz <= endZ; cz++) {
                Object[] chunkItems = chunkedItems.get(ChunkUtils.getChunkIndex(cx, cz));
                if (chunkItems != null) {
                    for (Object item : chunkItems) {
                        T boundedItem = (T)item;

                        double thisDistance = origin.distanceSquared(boundedItem.center());
                        if (thisDistance <= distance * distance) {
                            consumer.accept(boundedItem);
                        }
                    }
                }
            }
        }
    }

    @Override
    public @NotNull Optional<T> atPoint(int x, int y, int z) {
        Object[] chunkItems = chunkedItems.get(ChunkUtils.getChunkIndex(x >> 4, z >> 4));
        if (chunkItems == null) {
            return Optional.empty();
        }

        for (Object item : chunkItems) {
            T boundedItem = (T)item;
            for (Bounds3I bounds : boundedItem.bounds()) {
                if (bounds.contains(x, y, z)) {
                    return Optional.of(boundedItem);
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
