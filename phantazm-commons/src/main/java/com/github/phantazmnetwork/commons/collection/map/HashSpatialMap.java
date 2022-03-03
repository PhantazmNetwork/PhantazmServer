package com.github.phantazmnetwork.commons.collection.map;

import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;

/**
 * Naive implementation of {@link SpatialMap}. Values are stored in a {@link Object2ObjectRBTreeMap}, whose key is an
 * object encapsulating a 3D integer vector.
 * @param <TValue> the value stored in this map
 */
public class HashSpatialMap<TValue> implements SpatialMap<TValue> {
    private static record Point(int x, int y, int z) {
        private static final int PRIME = 31;

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Point point) {
                return x == point.x && y == point.y && z == point.z;
            }

            return false;
        }

        @Override
        public int hashCode() {
            int hash = PRIME + x;
            hash = PRIME * hash + y;
            return PRIME * hash + z;
        }
    }

    private final Map<Point, TValue> mappings = new Object2ObjectRBTreeMap<>();

    @Override
    public TValue get(int x, int y, int z) {
        return mappings.get(new Point(x, y, z));
    }

    @Override
    public void put(int x, int y, int z, TValue value) {
        mappings.put(new Point(x, y, z), value);
    }

    @Override
    public boolean containsKey(int x, int y, int z) {
        return mappings.containsKey(new Point(x, y, z));
    }

    @Override
    public void remove(int x, int y, int z) {
        mappings.remove(new Point(x, y, z));
    }

    @Override
    public int size() {
        return mappings.size();
    }

    @Override
    public @NotNull Iterator<TValue> iterator() {
        return mappings.values().iterator();
    }
}