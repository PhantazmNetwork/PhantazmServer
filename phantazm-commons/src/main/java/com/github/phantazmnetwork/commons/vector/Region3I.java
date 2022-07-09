package com.github.phantazmnetwork.commons.vector;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Represents a 3-dimensional bounding box with integer lengths. This class implements {@link Iterable}; when iterated,
 * it returns a value for each integer vector its bounds enclose. The returned vectors may be mutable, for performance.
 */
public interface Region3I extends Iterable<Vec3I> {
    /**
     * The "origin" vector of this region. This is the corner closest to ⟨-∞, -∞, -∞⟩.
     * @return the origin vector for this region
     */
    @NotNull Vec3I origin();

    /**
     * The "length" vectors for this region. These are always positive or zero, and when added to the origin vector
     * produce the maximum corner for this region (the corner closest to ⟨∞, ∞, ∞⟩).
     * @return the length vectors for this region
     */
    @NotNull Vec3I lengths();

    /**
     * Returns the "max" vector of this region. This is the sum of the origin and lengths vector.
     * @return the max vector of this region
     */
    default @NotNull Vec3I getMax() {
        return origin().add(lengths());
    }

    /**
     * Returns the exact center of this region as a double vector.
     * @return the center of this region
     */
    default @NotNull Vec3D getCenter() {
        Vec3I origin = origin();
        Vec3I lengths = lengths();

        return Vec3D.of(origin.getX() + ((double)lengths.getX() / 2D), origin.getY() +
                ((double)lengths.getY() / 2D), origin.getZ() + ((double)lengths.getZ() / 2D));
    }

    /**
     * Computes the smallest possible Region3I that can enclose all the given regions.
     * @param regions the regions a new Region3I must enclose
     * @return a new Region3I that must enclose all regions
     * @throws IllegalArgumentException if regions is empty
     * @throws NullPointerException if regions is null or contains a null element
     */
    static @NotNull Region3I enclosing(Region3I @NotNull ... regions) {
        if(regions.length == 0) {
            throw new IllegalArgumentException("Must provide at least one region");
        }

        Region3I first = regions[0];
        //shortcut
        if(regions.length == 1) {
            return first;
        }

        Vec3I firstOrigin = first.origin();
        Vec3I firstMax = first.getMax();

        int minX = firstOrigin.getX();
        int minY = firstOrigin.getY();
        int minZ = firstOrigin.getZ();

        int maxX = firstMax.getX();
        int maxY = firstMax.getY();
        int maxZ = firstMax.getZ();

        for(int i = 1; i < regions.length; i++) {
            Region3I sample = regions[i];
            Vec3I sampleOrigin = sample.origin();
            Vec3I sampleMax = sample.getMax();

            minX = Math.min(minX, sampleOrigin.getX());
            minY = Math.min(minY, sampleOrigin.getY());
            minZ = Math.min(minZ, sampleOrigin.getZ());

            maxX = Math.max(maxX, sampleMax.getX());
            maxY = Math.max(maxY, sampleMax.getY());
            maxZ = Math.max(maxZ, sampleMax.getZ());
        }

        return new BasicRegion3I(Vec3I.of(minX, minY, minZ), Vec3I.of(maxX - minX, maxY - minY, maxZ - minZ));
    }

    /**
     * Produces a new Region3I that encompasses both of the given vectors, whose coordinates will be considered relative
     * to the origin ⟨0, 0, 0⟩.
     * @param first the fist vector to encompass
     * @param second the second vector to encompass
     * @return a new Region3I encompassing both vectors
     */
    static @NotNull Region3I encompassing(@NotNull Vec3I first, @NotNull Vec3I second) {
        return encompassing(first, second, Vec3I.ORIGIN);
    }

    /**
     * Produces a new Region3I that encompasses both of the given vectors, whose coordinates will be considered relative
     * to the third vector.
     * @param first the fist vector to encompass
     * @param second the second vector to encompass
     * @param relativeTo the vector to which {@code first} and {@code second} will be considered relative to
     * @return a new Region3I encompassing both vectors
     */
    static @NotNull Region3I encompassing(@NotNull Vec3I first, @NotNull Vec3I second, @NotNull Vec3I relativeTo) {
        int x = Math.min(first.getX(), second.getX()) - relativeTo.getX();
        int y = Math.min(first.getY(), second.getY()) - relativeTo.getY();
        int z = Math.min(first.getZ(), second.getZ()) - relativeTo.getZ();

        int dX = Math.abs(first.getX() - second.getX()) + 1;
        int dY = Math.abs(first.getY() - second.getY()) + 1;
        int dZ = Math.abs(first.getZ() - second.getZ()) + 1;

        return new BasicRegion3I(Vec3I.of(x, y, z), Vec3I.of(dX, dY, dZ));
    }

    /**
     * Produces a new Region3I from "normalizing" the given origin and lengths vectors. The lengths may be negative, in
     * which case the new origin vector will differ from the original, but the result will still represent the same
     * bounding box.
     * @param origin the origin vector
     * @param lengths the lengths vector (components may be negative)
     * @param relativeTo the vector to which origin is considered relative to
     * @return a new Region3I with non-negative lengths, representing the same bounding box
     */
    static @NotNull Region3I normalized(@NotNull Vec3I origin, @NotNull Vec3I lengths, @NotNull Vec3I relativeTo) {
        return encompassing(origin, origin.add(lengths).sub(1, 1, 1), relativeTo);
    }

    /**
     * Produces a new Region3I from "normalizing" the given origin and lengths vectors. The lengths may be negative, in
     * which case the new origin vector will differ from the original, but the result will still represent the same
     * bounding box. Vectors are considered relative to the origin ⟨0, 0, 0⟩.
     * @param origin the origin vector
     * @param lengths the lengths vector (components may be negative)
     * @return a new Region3I with non-negative lengths, representing the same bounding box
     */
    static @NotNull Region3I normalized(@NotNull Vec3I origin, @NotNull Vec3I lengths) {
        return encompassing(origin, origin.add(lengths).sub(1, 1, 1), Vec3I.ORIGIN);
    }

    /**
     * Check if two bounding boxes overlap each other. All lengths should be non-negative, all origins should represent
     * the corner closest to ⟨-∞, -∞, -∞⟩ for their respective bounding box.
     * @param oX1 the X-component of the origin vector of the first bounding box
     * @param oY1 the Y-component of the origin vector of the first bounding box
     * @param oZ1 the Z-component of the origin vector of the first bounding box
     * @param lX1 the X-length of the first bounding box
     * @param lY1 the Y-length of the first bounding box
     * @param lZ1 the Z-length of the first bounding box
     * @param oX2 the X-component of the origin vector of the second bounding box
     * @param oY2 the Y-component of the origin vector of the second bounding box
     * @param oZ2 the Z-component of the origin vector of the second bounding box
     * @param lX2 the X-length of the second bounding box
     * @param lY2 the Y-length of the second bounding box
     * @param lZ2 the Z-length of the second bounding box
     * @return true if the bounding boxes overlap, false otherwise
     */
    static boolean overlaps(int oX1, int oY1, int oZ1, int lX1, int lY1, int lZ1,
                            int oX2, int oY2, int oZ2, int lX2, int lY2, int lZ2) {
        int nx1 = oX1 + lX1;
        int ny1 = oY1 + lY1;
        int nz1 = oZ1 + lZ1;

        int nx2 = oX2 + lX2;
        int ny2 = oY2 + lY2;
        int nz2 = oZ2 + lZ2;

        return Math.min(oX1, nx1) < Math.max(oX2, nx2) && Math.max(oX1, nx1) > Math.min(oX2, nx2)
                && Math.min(oY1, ny1) < Math.max(oY2, ny2) && Math.max(oY1, ny1) > Math.min(oY2, ny2)
                && Math.min(oZ1, nz1) < Math.max(oZ2, nz2) && Math.max(oZ1, nz1) > Math.min(oZ2, nz2);
    }

    /**
     * Checks if two bounding boxes overlap each other. All lengths should be non-negative, all origins should represent
     * the corner closest to ⟨-∞, -∞, -∞⟩ for their respective bounding box.
     * @param firstOrigin the origin of the first bounding box
     * @param firstLengths the lengths for the first bounding box
     * @param secondOrigin the origin of the second bounding box
     * @param secondLengths the lengths of the second bounding box
     * @return true if the bounding boxes overlap, false otherwise
     */
    static boolean overlaps(@NotNull Vec3I firstOrigin, @NotNull Vec3I firstLengths,
                            @NotNull Vec3I secondOrigin, @NotNull Vec3I secondLengths) {
        return overlaps(firstOrigin.getX(), firstOrigin.getY(), firstOrigin.getZ(), firstLengths.getX(), firstLengths
                .getY(), firstLengths.getZ(), secondOrigin.getX(), secondOrigin.getY(), secondOrigin.getZ(),
                secondLengths.getX(), secondLengths.getY(), secondLengths.getZ());
    }

    /**
     * Checks if two bounding boxes overlap each other.
     * @param first the first bounding box
     * @param second the second bounding box.
     * @return true if the bounding boxes overlap, false otherwise
     */
    static boolean overlaps(@NotNull Region3I first, @NotNull Region3I second) {
        return overlaps(first.origin(), first.lengths(), second.origin(), second.lengths());
    }

    /**
     * Returns the volume for this bounding box.
     * @return the volume of this bounding box
     */
    default int volume() {
        Vec3I lengths = lengths();
        return lengths.getX() * lengths.getY() * lengths.getZ();
    }

    /**
     * Checks if this bounding box overlaps another.
     * @param other the other bounding box to check against
     * @return true if the bounding boxes overlap, false otherwise
     */
    default boolean overlaps(@NotNull Region3I other) {
        return overlaps(this, other);
    }

    /**
     * Determines if this Region overlaps the given vector position.
     * @param position the position to check for overlap
     * @return true if the position overlaps, false otherwise
     */
    default boolean contains(@NotNull Vec3I position) {
        Vec3I origin = origin();
        if(position.getX() >= origin.getX() && position.getY() >= origin.getY() && position.getZ() >= origin.getZ()) {
            Vec3I end = origin.add(lengths());
            return position.getX() < end.getX() && position.getY() < end.getY() && position.getZ() < end.getZ();
        }

        return false;
    }

    @Override
    default @NotNull Iterator<Vec3I> iterator() {
        Vec3I origin = origin();
        Vec3I lengths = lengths();

        return new Iterator<>() {
            private int x = origin.getX();
            private int y = origin.getY();
            private int z = origin.getZ();

            private final int xEnd = origin.getX() + lengths.getX();
            private final int yEnd = origin.getY() + lengths.getY();
            private final int zEnd = origin.getZ() + lengths.getZ();

            private final Vec3I local = Vec3I.threadLocal();

            @Override
            public boolean hasNext() {
                return z < zEnd;
            }

            @Override
            public Vec3I next() {
                int curX = x;
                int curY = y;
                int curZ = z;

                if(++x >= xEnd) {
                    curX = x = origin.getX();
                    if(++y >= yEnd) {
                        curY = y = origin.getY();
                        if(++z > zEnd) {
                            throw new NoSuchElementException();
                        }
                    }
                }

                local.set(curX, curY, curZ);
                return local;
            }
        };
    }
}
