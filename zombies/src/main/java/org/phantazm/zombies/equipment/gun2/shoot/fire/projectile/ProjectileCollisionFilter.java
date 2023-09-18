package org.phantazm.zombies.equipment.gun2.shoot.fire.projectile;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ProjectileCollisionFilter {

    boolean shouldExplode(@NotNull Entity cause);

}
