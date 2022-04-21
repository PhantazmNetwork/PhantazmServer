package com.github.phantazmnetwork.neuron.bindings.minestom;

import net.minestom.server.collision.PhysicsResult;

@SuppressWarnings("UnstableApiUsage")
public final class PhysicsUtils {
    private PhysicsUtils() {
        throw new UnsupportedOperationException();
    }

    public static boolean hasCollision(PhysicsResult result) {
        return result.collisionX() || result.collisionY() || result.collisionZ();
    }
}
