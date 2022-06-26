package com.github.phantazmnetwork.commons.vector;

import org.jetbrains.annotations.NotNull;

public interface Region3I {
    @NotNull Vec3I origin();

    @NotNull Vec3I lengths();

    static @NotNull Region3I encompassing(@NotNull Vec3I first, @NotNull Vec3I second) {
        return encompassing(first, second, Vec3I.ORIGIN);
    }

    static @NotNull Region3I encompassing(@NotNull Vec3I first, @NotNull Vec3I second, @NotNull Vec3I origin) {
        int x = Math.min(first.getX(), second.getX()) - origin.getX();
        int y = Math.min(first.getY(), second.getY()) - origin.getY();
        int z = Math.min(first.getZ(), second.getZ()) - origin.getZ();

        int dX = Math.abs(first.getX() - second.getX()) + 1;
        int dY = Math.abs(first.getY() - second.getY()) + 1;
        int dZ = Math.abs(first.getZ() - second.getZ()) + 1;

        return new BasicRegion3I(Vec3I.of(x, y, z), Vec3I.of(dX, dY, dZ));
    }

    static @NotNull Region3I normalized(@NotNull Vec3I first, @NotNull Vec3I lengths, @NotNull Vec3I origin) {
        Vec3I second = Vec3I.of(first.getX() + lengths.getX(), first.getY() + lengths.getY(), first.getZ()
                + lengths.getZ());
        return encompassing(first, second, origin);
    }

    static @NotNull Region3I normalized(@NotNull Vec3I first, @NotNull Vec3I lengths) {
        Vec3I second = Vec3I.of(first.getX() + lengths.getX(), first.getY() + lengths.getY(), first.getZ()
                + lengths.getZ());
        return encompassing(first, second, Vec3I.ORIGIN);
    }

    static @NotNull boolean overlaps(int oX, int oY, int oZ, int lX, int lY, int lZ) {

    }
}
