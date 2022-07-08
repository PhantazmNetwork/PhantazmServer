package com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Finds the endpoint of a shot.
 */
@FunctionalInterface
public interface ShotEndpointSelector {

    /**
     * Finds the endpoint of a shot.
     * @param start The start position of the shot
     * @return The endpoint of the shot
     */
    @NotNull Optional<Point> getEnd(@NotNull Pos start);

}
