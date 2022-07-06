package com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint;

import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Optional;

public interface BlockIteration {

    @NotNull Optional<Vec> findEnd(@NotNull Instance instance, @NotNull Entity shooter, @NotNull Pos start,
                                   @NotNull Iterator<Point> it);

    @NotNull Keyed getData();

}
