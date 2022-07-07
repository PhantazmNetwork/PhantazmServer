package com.github.phantazmnetwork.zombies.equipment.gun.target.intersectionfinder;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@FunctionalInterface
public interface IntersectionFinder {

    @NotNull Optional<Vec> getHitLocation(@NotNull Entity entity, @NotNull Pos start);

}
