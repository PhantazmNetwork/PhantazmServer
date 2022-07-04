package com.github.phantazmnetwork.commons.vector;

import org.jetbrains.annotations.NotNull;

/**
 * <p>Represents a 3D integer vector (point). Contains some utility methods for working with vectors/points.</p>
 *
 * <p>Implementations must provide suitable overrides for {@link Object#equals(Object)} and {@link Object#hashCode()},
 * based only off of the vector's component values. Any two Vec3I instances must be considered equal if and only if each
 * of their components is equal. This allows for different Vec3I implementations to be compared against each other, and
 * ensures consistency with static methods like {@link Vec3I#equals(int, int, int, int, int, int)}.</p>
 */
public interface Vec3I extends Comparable<Vec3I> {
    /**
     * The x-component of this vector.
     * @return the x-component
     */
    int getX();

    /**
     * The y-component of this vector.
     * @return the y-component
     */
    int getY();

    /**
     * The z-component of this vector.
     * @return the z-component
     */
    int getZ();

    /**
     * Sets the x-component of this vector.
     * @param x the new value of the x-component
     * @throws UnsupportedOperationException if this vector is immutable
     */
    default void setX(int x) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the y-component of this vector.
     * @param y the new value of the x-component
     * @throws UnsupportedOperationException if this vector is immutable
     */
    default void setY(int y) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the z-component of this vector.
     * @param z the new value of the x-component
     * @throws UnsupportedOperationException if this vector is immutable
     */
    default void setZ(int z) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the x, y, and z components of this vector.
     * @param x the new value of the x-component
     * @param y the new value of the y-component
     * @param z the new value of the z-component
     * @throws UnsupportedOperationException if this vector is immutable
     */
    default @NotNull Vec3I set(int x, int y, int z) {
        throw new UnsupportedOperationException();
    }

    /**
     * A static, immutable vector representing the origin (0, 0, 0).
     */
    Vec3I ORIGIN = of(0, 0, 0);

    /**
     * <p>Returns an immutable Vec3I implementation. This may not always create a new vector; common values may (but are
     * not guaranteed to be) returned from a persistent cache.</p>
     *
     * @apiNote This method is threadsafe, as it relies only on the global {@link Vec3IPool} instance to supply cached
     * values.
     * @param x the x-component
     * @param y the y-component
     * @param z the z-component
     * @return a new, immutable Vec3I instance with the given components
     * @see ImmutableVec3I
     */
    static @NotNull Vec3I of(int x, int y, int z) {
        Vec3I cached = Vec3IPools.GLOBAL.fromCache(x, y, z);
        if(cached != null) {
            return cached;
        }

        return new ImmutableVec3I(x, y, z);
    }

    static @NotNull Vec3I mutable(int x, int y, int z) {
        return new MutableVec3I(x, y, z);
    }

    static @NotNull Vec3I mutable() {
        return new MutableVec3I(0, 0, 0);
    }

    /**
     * Returns the mutable thread-local Vec3I instance. This can be used to enable safe, performant optimization when
     * iterating many vectors; instead of creating a new vector every iteration, the iterator can update the vector's
     * state as needed.
     * @return the thread-local Vec3I instance for this thread
     */
    static @NotNull Vec3I threadLocal() {
        return Vec3IPools.THREAD_LOCAL.get();
    }

    /**
     * Returns a Vec3I created from taking the mathematical floor of each given {@code double} component before casting
     * to an integer.
     * @param x the x-component of the new vector, which will be floored
     * @param y the y-component of the new vector, which will be floored
     * @param z the z-component of the new vector, which will be floored
     * @return the floored vector
     */
    static @NotNull Vec3I floored(double x, double y, double z) {
        return of((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
    }

    /**
     * Returns a Vec3I created from taking the mathematical floor of each given {@code double} component of the provided
     * vector before casting to an integer.
     * @return the floored vector
     */
    static @NotNull Vec3I floored(@NotNull Vec3D other) {
        return floored(other.getX(), other.getY(), other.getZ());
    }

    /**
     * Determines if two points are equal.
     * @param x1 first x-component
     * @param y1 first y-component
     * @param z1 first z-component
     * @param x2 second x-component
     * @param y2 second y-component
     * @param z2 second z-component
     * @return true if the two points are the same, false otherwise
     */
    static boolean equals(int x1, int y1, int z1, int x2, int y2, int z2) {
        return x1 == x2 && y1 == y2 && z1 == z2;
    }

    /**
     * Computes the hashcode of an integer triplet.
     * @param x the x-component
     * @param y the y-component
     * @param z the z-component
     * @return the hash of the three integers
     */
    static int hashCode(int x, int y, int z) {
        int h = 7;
        h = 31 * h + x;
        h = 31 * h + y;
        return 31 * h + z;
    }

    default boolean isMutable() {
        return false;
    }

    /**
     * Returns an immutable copy of this vector if it is mutable. Otherwise, returns the same vector.
     * @return an immutable copy of this vector if it is mutable, otherwise the same vector
     */
    default @NotNull Vec3I immutable() {
        if(isMutable()) {
            return Vec3I.of(getX(), getY(), getZ());
        }

        return this;
    }

    default @NotNull Vec3I mutableCopy() {
        return new MutableVec3I(getX(), getY(), getZ());
    }

    /**
     * Compares two integer vectors. Vectors are compared first by x-value, the y, then z.
     * @param x1 first x-component
     * @param y1 first y-component
     * @param z1 first z-component
     * @param x2 second x-component
     * @param y2 second y-component
     * @param z2 second z-component
     * @return -1 if first &lt; second, 0 if first == second, 1 if first &gt; second
     */
    static int compare(int x1, int y1, int z1, int x2, int y2, int z2) {
        int xCompare = Integer.compare(x1, x2);
        if (xCompare == 0) {
            int yCompare = Integer.compare(y1, y2);
            if (yCompare == 0) {
                return Integer.compare(z1, z2);
            }

            return yCompare;
        }

        return xCompare;
    }

    /**
     * <p>Computes the <i>squared distance</i> between two points (x1, y1, z1) and (x2, y2, z2). This uses the standard
     * distance formula for two 3-dimensional points, but omits the square root for performance.</p>
     * @param x1 first x-component
     * @param y1 first y-component
     * @param z1 first z-component
     * @param x2 second x-component
     * @param y2 second y-component
     * @param z2 second z-component
     * @return the squared distance between the point {@code first} and the point {@code second}
     */
    static double squaredDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
        int dX = x1 - x2;
        int dY = y1 - y2;
        int dZ = z1 - z2;
        return dX * dX + dY * dY + dZ * dZ;
    }

    /**
     * <p>Computes the <i>distance</i> between two points (x1, y1, z1) and (x2, y2, z2). This uses the standard
     * distance formula for two 3-dimensional points.</p>
     * @param x1 first x-component
     * @param y1 first y-component
     * @param z1 first z-component
     * @param x2 second x-component
     * @param y2 second y-component
     * @param z2 second z-component
     * @return the distance between the point {@code first} and the point {@code second}
     */
    static double distance(int x1, int y1, int z1, int x2, int y2, int z2) {
        return Math.sqrt(squaredDistance(x1, y1, z1, x2, y2, z2));
    }

    /**
     * Computes the squared magnitude (length) of the given vector.
     * @param x the x-component
     * @param y the y-component
     * @param z the z-component
     * @return the squared magnitude of the vector
     */
    static double magnitudeSquared(int x, int y, int z) {
        return x * x + y * y + z * z;
    }

    /**
     * Computes the magnitude (length) of the given vector.
     * @param x the x-component
     * @param y the y-component
     * @param z the z-component
     * @return the magnitude of the vector
     */
    static double magnitude(int x, int y, int z) {
        return Math.sqrt(magnitudeSquared(x, y, z));
    }

    /**
     * <p>Computes the <i>squared distance</i> between this vector and (x1, y1, z1). This uses the standard distance
     * formula for two 3-dimensional points, but omits the square root for performance.</p>
     * @param x first x-component
     * @param y first y-component
     * @param z first z-component
     * @return the squared distance between this vector and another
     */
    default double squaredDistance(int x, int y, int z) {
        return squaredDistance(getX(), getY(), getZ(), x, y, z);
    }

    /**
     * <p>Computes the <i>squared distance</i> between this vector and another. This uses the standard distance
     * formula for two 3-dimensional points, but omits the square root for performance.</p>
     * @param other the other vector
     * @return the squared distance between this vector and another
     */
    default double squaredDistance(@NotNull Vec3I other) {
        return squaredDistance(other.getX(), other.getY(), other.getZ());
    }

    /**
     * Computes the squared magnitude (length) of this vector.
     * @return the squared magnitude of the vector
     */
    default double magnitudeSquared() {
        return magnitude(getX(), getY(), getZ());
    }

    /**
     * Computes the magnitude (length) of this vector.
     * @return the magnitude of the vector
     */
    default double magnitude() {
        return Math.sqrt(magnitudeSquared());
    }

    default @NotNull Vec3I mul(int x, int y, int z) {
        return op(getX() * x, getY() * y, getZ() * z);
    }

    default @NotNull Vec3I mul(@NotNull Vec3I other) {
        return mul(other.getX(), other.getY(), other.getZ());
    }

    default @NotNull Vec3I div(int x, int y, int z) {
        return op(getX() / x, getY() / y, getZ() / z);
    }

    default @NotNull Vec3I div(@NotNull Vec3I other) {
        return div(other.getX(), other.getY(), other.getZ());
    }

    default @NotNull Vec3I add(int x, int y, int z) {
        return op(getX() + x, getY() + y, getZ() + z);
    }

    default @NotNull Vec3I add(@NotNull Vec3I other) {
        return add(other.getX(), other.getY(), other.getZ());
    }

    default @NotNull Vec3I sub(int x, int y, int z) {
        return op(getX() - x, getY() - y, getZ() - z);
    }

    default @NotNull Vec3I sub(@NotNull Vec3I other) {
        return sub(other.getX(), other.getY(), other.getZ());
    }

    @NotNull Vec3I op(int x, int y, int z);
}