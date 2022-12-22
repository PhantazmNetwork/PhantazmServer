package org.phantazm.core;

import com.github.steanky.vector.Vec3D;
import com.github.steanky.vector.Vec3I;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

/**
 * Utilities for converting phantazm-commons Vec3X objects to Minestom {@link Point} and vice-versa.
 */
public final class VecUtils {
    private VecUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts the given {@link Point} to a {@link Vec3I}, using the point's block coordinates (floored position).
     *
     * @param point the point to convert
     * @return a Vec3I representing the block position of the given point
     */
    public static @NotNull Vec3I toBlockInt(@NotNull Point point) {
        return Vec3I.immutable(point.blockX(), point.blockY(), point.blockZ());
    }

    /**
     * Converts the given {@link Point} to a {@link Vec3D}.
     *
     * @param point the point to convert
     * @return a Vec3D representing the position of the point
     */
    public static @NotNull Vec3D toDouble(@NotNull Point point) {
        return Vec3D.immutable(point.x(), point.y(), point.z());
    }

    public static @NotNull Pos toPos(@NotNull Vec3I vec) {
        return new Pos(vec.x(), vec.y(), vec.z());
    }

    public static @NotNull Vec toVec(@NotNull Vec3I vec) {
        return new Vec(vec.x(), vec.y(), vec.z());
    }

    /**
     * Converts the given {@link Vec3I} to an equivalent {@link Point}.
     *
     * @param vec the Vec3I to convert
     * @return a new Point ({@link Vec}) from the components of the given Vec3I
     */
    public static @NotNull Point toPoint(@NotNull Vec3I vec) {
        return toVec(vec);
    }

    public static @NotNull Pos toPos(@NotNull Vec3D vec) {
        return new Pos(vec.x(), vec.y(), vec.z());
    }

    public static @NotNull Vec toVec(@NotNull Vec3D vec) {
        return new Vec(vec.x(), vec.y(), vec.z());
    }

    /**
     * Converts the given {@link Vec3D} to an equivalent {@link Point}.
     *
     * @param vec the Vec3D to convert
     * @return a new Point ({@link Vec}) from the components of the given Vec3D
     */
    public static @NotNull Point toPoint(@NotNull Vec3D vec) {
        return new Vec(vec.x(), vec.y(), vec.z());
    }
}
