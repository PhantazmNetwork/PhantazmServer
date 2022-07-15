package com.github.phantazmnetwork.zombies.equipment.gun.target.headshot;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * Tests whether the intersection of an {@link Entity} and a shot should result in a headshot.
 */
@FunctionalInterface
public interface HeadshotTester {

    /**
     * Tests whether the intersection of an {@link Entity} and a shot should result in a headshot.
     *
     * @param shooter      The shooter {@link Entity}
     * @param entity       The target {@link Entity}
     * @param intersection The intersection between the target and the shot
     * @return Whether the intersection should result in a headshot
     */
    boolean isHeadshot(@NotNull Entity shooter, @NotNull Entity entity, @NotNull Point intersection);

}
