package com.github.phantazmnetwork.commons.collection.map;

public interface SpatialMap<TValue> {
    TValue get(int x, int y, int z);

    void put(int x, int y, int z, TValue value);

    boolean containsKey(int x, int y, int z);
}
