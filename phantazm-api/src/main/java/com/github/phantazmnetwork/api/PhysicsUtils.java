package com.github.phantazmnetwork.api;

import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.collision.Shape;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for {@link PhysicsResult} objects. This class cannot be instantiated.
 */
@SuppressWarnings("UnstableApiUsage")
public final class PhysicsUtils {
    private PhysicsUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Determines if the given {@link PhysicsResult} represents a collision anywhere. Equivalent to
     * {@code result.collisionX() || result.collisionY() || result.collisionZ()}.
     * @param result the result to check if any collisions were found
     * @return {@code true} if the given PhysicsResult has any collisions, {@code false} otherwise
     */
    public static boolean hasCollision(@NotNull PhysicsResult result) {
        return result.collisionX() || result.collisionY() || result.collisionZ();
    }

    /**
     * Can be used to determine if the given shape has any collision or not. Equivalent to
     * {@code !shape.relativeEnd().isZero()}.
     * @param shape the shape to test
     * @return {@code true} if the shape is collidable, {@code false} otherwise
     */
    public static boolean isCollidable(@NotNull Shape shape) {
        return !shape.relativeEnd().isZero();
    }

    /**
     * Can be used to determine if a given {@link Shape} is "tall" (has a height greater than 1). Equivalent to
     * {@code shape.relativeEnd().y() > 1}.
     * @param shape the shape to test
     * @return {@code true} if the shape is tall, {@code false} otherwise
     */
    public static boolean isTall(@NotNull Shape shape) {
        return shape.relativeEnd().y() > 1;
    }
}
