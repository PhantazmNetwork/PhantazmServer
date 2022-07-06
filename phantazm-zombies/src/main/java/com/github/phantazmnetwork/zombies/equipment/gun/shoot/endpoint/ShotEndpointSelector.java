package com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@FunctionalInterface
public interface ShotEndpointSelector {

    @NotNull Optional<Point> getEnd(@NotNull Pos start);

}
