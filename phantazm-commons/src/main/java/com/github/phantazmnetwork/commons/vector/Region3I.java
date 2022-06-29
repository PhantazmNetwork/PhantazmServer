package com.github.phantazmnetwork.commons.vector;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Represents a 3-dimensional bounding box with integer lengths. This class implements {@link Iterable}; when iterated,
 * it returns a value for each integer vector its bounds enclose.
 */
public interface Region3I extends Iterable<Vec3I> {
    /**
     * The "origin" vector of this region. This is the corner closest to ⟨-∞, -∞, -∞⟩.
     * @return the origin vector for this region
     */
    @NotNull Vec3I getOrigin();

    /**
     * The "length" vectors for this region. These are always positive or zero, and when added to the origin vector
     * produce the maximum corner for this region (the corner closest to ⟨∞, ∞, ∞⟩).
     * @return the length vectors for this region
     */
    @NotNull Vec3I getLengths();


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
        Vec3I second = Vec3I.of(origin.getX() + lengths.getX() - 1, origin.getY() + lengths.getY() - 1, origin
                .getZ() + lengths.getZ() - 1);
        return encompassing(origin, second, relativeTo);
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
        Vec3I second = Vec3I.of(origin.getX() + lengths.getX() - 1, origin.getY() + lengths.getY() - 1, origin
                .getZ() + lengths.getZ() - 1);
        return encompassing(origin, second, Vec3I.ORIGIN);
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
        return overlaps(first.getOrigin(), first.getLengths(), second.getOrigin(), second.getLengths());
    }

    /**
     * Returns the volume for this bounding box.
     * @return the volume of this bounding box
     */
    default int volume() {
        Vec3I lengths = getLengths();
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

    @Override
    default @NotNull Iterator<Vec3I> iterator() {
        Vec3I origin = getOrigin();
        Vec3I lengths = getLengths();

        return new Iterator<>() {
            private int x = origin.getX();
            private int y = origin.getY();
            private int z = origin.getZ();

            private final int xEnd = origin.getX() + lengths.getX();
            private final int yEnd = origin.getY() + lengths.getY();
            private final int zEnd = origin.getZ() + lengths.getZ();

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

                return Vec3I.of(curX, curY, curZ);
            }
        };
    }
}
