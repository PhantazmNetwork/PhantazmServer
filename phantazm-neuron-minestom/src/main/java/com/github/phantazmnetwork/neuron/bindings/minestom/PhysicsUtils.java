package com.github.phantazmnetwork.neuron.bindings.minestom;

import net.minestom.server.collision.PhysicsResult;
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
}
