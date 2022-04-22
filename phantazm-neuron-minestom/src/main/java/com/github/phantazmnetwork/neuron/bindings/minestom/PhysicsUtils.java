package com.github.phantazmnetwork.neuron.bindings.minestom;

import net.minestom.server.collision.PhysicsResult;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public final class PhysicsUtils {
    private PhysicsUtils() {
        throw new UnsupportedOperationException();
    }

    public static boolean hasCollision(@NotNull PhysicsResult result) {
        return result.collisionX() || result.collisionY() || result.collisionZ();
    }
}
