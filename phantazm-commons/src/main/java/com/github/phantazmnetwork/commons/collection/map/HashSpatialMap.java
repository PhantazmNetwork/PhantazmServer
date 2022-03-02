package com.github.phantazmnetwork.commons.collection.map;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Naive implementation of {@link SpatialMap}. Values are stored in a simple {@link HashMap}, whose key is an object
 * encapsulating a 3D integer vector. This is not a particularly time or space-efficient method of storage, but it is
 * useful as a comparison against more refined techniques.
 * @param <TValue> the value stored in this map
 */
public class HashSpatialMap<TValue> implements SpatialMap<TValue> {
    private static record Point(int x, int y, int z) {
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Point point) {
                return x == point.x && y == point.y && z == point.z;
            }

            return false;
        }

        @Override
        public int hashCode() {
            int hash = 1;

            hash = 31 * hash + x;
            hash = 31 * hash + y;
            hash = 31 * hash + z;
            return hash;
        }
    }

    private final Map<Point, TValue> mappings = new HashMap<>();

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