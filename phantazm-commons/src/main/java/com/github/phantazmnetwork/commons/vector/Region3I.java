package com.github.phantazmnetwork.commons.vector;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

public interface Region3I {
    @NotNull Vec3I getOrigin();

    @NotNull Vec3I getLengths();

    static @NotNull Region3I encompassing(@NotNull Vec3I first, @NotNull Vec3I second) {
        return encompassing(first, second, Vec3I.ORIGIN);
    }

    static @NotNull Region3I encompassing(@NotNull Vec3I first, @NotNull Vec3I second, @NotNull Vec3I relativeTo) {
        int x = Math.min(first.getX(), second.getX()) - relativeTo.getX();
        int y = Math.min(first.getY(), second.getY()) - relativeTo.getY();
        int z = Math.min(first.getZ(), second.getZ()) - relativeTo.getZ();

        int dX = Math.abs(first.getX() - second.getX()) + 1;
        int dY = Math.abs(first.getY() - second.getY()) + 1;
        int dZ = Math.abs(first.getZ() - second.getZ()) + 1;

        return new BasicRegion3I(Vec3I.of(x, y, z), Vec3I.of(dX, dY, dZ));
    }

    static @NotNull Region3I normalized(@NotNull Vec3I first, @NotNull Vec3I lengths, @NotNull Vec3I relativeTo) {
        Vec3I second = Vec3I.of(first.getX() + lengths.getX() - 1, first.getY() + lengths.getY() - 1, first
                .getZ() + lengths.getZ() - 1);
        return encompassing(first, second, relativeTo);
    }

    static @NotNull Region3I normalized(@NotNull Vec3I first, @NotNull Vec3I lengths) {
        Vec3I second = Vec3I.of(first.getX() + lengths.getX() - 1, first.getY() + lengths.getY() - 1, first
                .getZ() + lengths.getZ() - 1);
        return encompassing(first, second, Vec3I.ORIGIN);
    }

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

    static boolean overlaps(@NotNull Vec3I firstOrigin, @NotNull Vec3I firstLengths,
                            @NotNull Vec3I secondOrigin, @NotNull Vec3I secondLengths) {
        return overlaps(firstOrigin.getX(), firstOrigin.getY(), firstOrigin.getZ(), firstLengths.getX(), firstLengths
                .getY(), firstLengths.getZ(), secondOrigin.getX(), secondOrigin.getY(), secondOrigin.getZ(),
                secondLengths.getX(), secondLengths.getY(), secondLengths.getZ());
    }

    static boolean overlaps(@NotNull Region3I first, @NotNull Region3I second) {
        return overlaps(first.getOrigin(), first.getLengths(), second.getOrigin(), second.getLengths());
    }

    default int volume() {
        Vec3I lengths = getLengths();
        return lengths.getX() * lengths.getY() * lengths.getZ();
    }

    default boolean overlaps(@NotNull Region3I other) {
        return overlaps(this, other);
    }

    default @NotNull Iterator<Vec3I> blockIterator() {
        return new Iterator<>() {
            private final Vec3I origin = Region3I.this.getOrigin();
            private final Vec3I lengths = Region3I.this.getLengths();
            private int x = origin.getX();
            private int y = origin.getY();
            private int z = origin.getZ();

            @Override
            public boolean hasNext() {
                return z < (z + lengths.getZ());
            }

            @Override
            public Vec3I next() {
                int curX = x;
                int curY = y;
                int curZ = z;

                if(x++ >= origin.getX() + lengths.getX()) {
                    x = origin.getX();
                    if((curY = y++) >= (origin.getY() + lengths.getY())) {
                        y = origin.getY();
                        if(z++ >= (origin.getZ() + lengths.getZ())) {
                            throw new NoSuchElementException();
                        }
                    }
                }

                return Vec3I.of(curX, curY, curZ);
            }
        };
    }
}
