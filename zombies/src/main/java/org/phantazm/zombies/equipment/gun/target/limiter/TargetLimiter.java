package org.phantazm.zombies.equipment.gun.target.limiter;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.shoot.GunHit;

import java.util.List;

@FunctionalInterface
public interface TargetLimiter {
    @NotNull
    List<GunHit> limitTargets(@NotNull Entity shooter, @NotNull Gun gun, @NotNull Pos start, @NotNull List<GunHit> targets);
}
