package com.github.phantazmnetwork.zombies.equipment.gun.target.limiter;

import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TargetLimiter {

    @NotNull List<Pair<? extends LivingEntity, Vec>> limitTargets(@NotNull Pos start,
                                                                  @NotNull List<Pair<? extends LivingEntity, Vec>> targets);

}
