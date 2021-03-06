package com.github.phantazmnetwork.api;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3F;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

/**
 * Utilities for converting phantazm-commons Vec3X objects to Minestom {@link Point} and vice-versa.
 */
public final class VecUtils {
    private VecUtils() { throw new UnsupportedOperationException(); }

    /**
     * Converts the given {@link Point} to a {@link Vec3I}, using the point's block coordinates (floored position).
     * @param point the point to convert
     * @return a Vec3I representing the block position of the given point
     */
    public static @NotNull Vec3I toBlockInt(@NotNull Point point) {
        return Vec3I.of(point.blockX(), point.blockY(), point.blockZ());
    }

    /**
     * Converts the given {@link Point} to a {@link Vec3F}, casting each coordinate to a float.
     * @param point the point to convert
     * @return a Vec3F representing the position of the point, after casting each double component to a float
     */
    public static @NotNull Vec3F toFloat(@NotNull Point point) {
        return Vec3F.of((float) point.x(), (float) point.y(), (float) point.z());
    }

    /**
     * Converts the given {@link Point} to a {@link Vec3D}.
     * @param point the point to convert
     * @return a Vec3D representing the position of the point
     */
    public static @NotNull Vec3D toDouble(@NotNull Point point) {
        return Vec3D.of(point.x(), point.y(), point.z());
    }

    /**
     * Converts the given {@link Vec3I} to an equivalent {@link Point}.
     * @param vec the Vec3I to convert
     * @return a new Point ({@link Vec}) from the components of the given Vec3I
     */
    public static @NotNull Point toPoint(@NotNull Vec3I vec) {
        return new Vec(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * Converts the given {@link Vec3F} to an equivalent {@link Point}.
     * @param vec the Vec3F to convert
     * @return a new Point ({@link Vec}) from the components of the given Vec3F
     */
    public static @NotNull Point toPoint(@NotNull Vec3F vec) {
        return new Vec(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * Converts the given {@link Vec3D} to an equivalent {@link Point}.
     * @param vec the Vec3D to convert
     * @return a new Point ({@link Vec}) from the components of the given Vec3D
     */
    public static @NotNull Point toPoint(@NotNull Vec3D vec) {
        return new Vec(vec.getX(), vec.getY(), vec.getZ());
    }
}
