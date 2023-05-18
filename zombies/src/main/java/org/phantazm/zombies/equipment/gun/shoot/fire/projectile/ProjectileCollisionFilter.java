package org.phantazm.zombies.equipment.gun.shoot.fire.projectile;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * Tests whether a projectile should explode when it hits an {@link Entity}.
 */
@FunctionalInterface
public interface ProjectileCollisionFilter {

    /**
     * Tests whether a projectile should explode when it hits an {@link Entity}.
     *
     * @param cause The {@link Entity} that the projectile hit
     * @return Whether the projectile should explode
     */
    boolean shouldExplode(@NotNull Entity cause);

}
