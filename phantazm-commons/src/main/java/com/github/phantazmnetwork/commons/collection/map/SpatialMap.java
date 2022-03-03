package com.github.phantazmnetwork.commons.collection.map;

/**
 * Associates 3D integer vectors with some arbitrary value type. This can be thought of as a specialization of
 * {@link java.util.Map}, using three {@code int} values as a key.
 * @param <TValue> the value stored in the map
 */
public interface SpatialMap<TValue> extends Iterable<TValue> {
    /**
     * Gets the value stored under the provided coordinate.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @return the value stored under the coordinate, which will be null if no such value exists (or if the stored
     * value is itself null)
     */
    TValue get(int x, int y, int z);

    /**
     * Associates a value with a coordinate.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @param value the value to associate with the coordinate
     */
    void put(int x, int y, int z, TValue value);

    /**
     * Returns {@code true} if a value is associated with the coordinate. The value may be null.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     * @return {@code true} if a value has been associated with this coordinate, {@code false} otherwise
     */
    boolean containsKey(int x, int y, int z);

    /**
     * Removes a value from this map, given its coordinate.
     * @param x the x value
     * @param y the y value
     * @param z the z value
     */
    void remove(int x, int y, int z);

    /**
     * Returns the size of this map (the number of values that have been registered).
     * @return the size of this map
     */
    int size();
}
