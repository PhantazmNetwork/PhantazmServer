package com.github.phantazmnetwork.commons.minestom.vector;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3F;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

/**
 * Utilities for converting phantazm-commons Vec3X objects to Minestom {@link Point} and vice-versa.
 */
public final class VecUtils {
    private VecUtils() { throw new UnsupportedOperationException(); }

    public static @NotNull Vec3I toBlockInt(@NotNull Point point) {
        return Vec3I.of(point.blockX(), point.blockY(), point.blockZ());
    }

    public static @NotNull Vec3F toFloat(@NotNull Point point) {
        return Vec3F.of((float) point.x(), (float) point.y(), (float) point.z());
    }

    public static @NotNull Vec3D toDouble(@NotNull Point point) {
        return Vec3D.of(point.x(), point.y(), point.z());
    }

    public static @NotNull Point toPoint(@NotNull Vec3I vec) {
        return new Pos(vec.getX(), vec.getY(), vec.getZ());
    }

    public static @NotNull Point toPoint(@NotNull Vec3F vec) {
        return new Pos(vec.getX(), vec.getY(), vec.getZ());
    }

    public static @NotNull Point toPoint(@NotNull Vec3D vec) {
        return new Pos(vec.getX(), vec.getY(), vec.getZ());
    }
}
