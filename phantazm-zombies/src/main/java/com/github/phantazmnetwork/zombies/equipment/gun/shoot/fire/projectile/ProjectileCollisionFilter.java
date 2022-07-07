package com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.projectile;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface ProjectileCollisionFilter {

    boolean shouldExplode(@NotNull Entity cause);

}
