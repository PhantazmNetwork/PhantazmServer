package com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Optional;

/**
 * A method of iteration over {@link Point}s.
 * Implementations may choose to stop iterating early and whether block ray tracing should be used.
 */
@FunctionalInterface
public interface BlockIteration {

    /**
     * Finds the end of a block iteration.
     *
     * @param instance The {@link Instance} to iterate in
     * @param shooter  The shooter of the shot
     * @param start    The start {@link Point} of the shot
     * @param it       The {@link Iterator} of {@link Point}s to iterate over
     * @return The end {@link Vec} of the iteration
     */
    @NotNull Optional<Vec> findEnd(@NotNull Instance instance, @NotNull Entity shooter, @NotNull Pos start,
            @NotNull Iterator<Point> it);

}
