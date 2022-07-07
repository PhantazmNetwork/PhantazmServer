package com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.directional;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@FunctionalInterface
public interface DirectionalEntityFinder {

    @NotNull Collection<LivingEntity> findEntities(@NotNull Instance instance, @NotNull Pos start, @NotNull Point end);

}