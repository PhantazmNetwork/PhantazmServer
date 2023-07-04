package org.phantazm.zombies.mob.skill.hit_action;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.mob.PhantazmMob;

public interface ProjectileHitEntityAction {
    void perform(@Nullable PhantazmMob shooter, @NotNull Entity projectile, @NotNull Entity target);
}
