package com.github.phantazmnetwork.zombies.equipment.gun.shoot.blockiteration;

import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

/**
 * A method of iteration over {@link Point}s.
 * Implementations may choose to stop iterating early and whether block ray tracing should be used.
 */
@FunctionalInterface
public interface BlockIteration {

    @NotNull Context createContext();

    interface Context {

        @SuppressWarnings("UnstableApiUsage")
        boolean isValidEndpoint(@NotNull Point blockLocation, @NotNull Shape shape);

        @SuppressWarnings("UnstableApiUsage")
        boolean isValidIntersection(@NotNull Vec intersection, @NotNull Shape shape);

    }

}
