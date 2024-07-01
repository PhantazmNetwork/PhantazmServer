package org.phantazm.zombies.equipment.gun.target.limiter;

import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.Gun;

import java.util.List;

@FunctionalInterface
public interface TargetLimiter {
    @NotNull
    List<Pair<? extends LivingEntity, Vec>> limitTargets(@NotNull Entity shooter, @NotNull Gun gun, @NotNull Pos start,
        @NotNull List<Pair<? extends LivingEntity, Vec>> targets);
}
