package com.github.phantazmnetwork.zombies.equipment.gun.target.limiter;

import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Limits the chosen targets to shoot from a {@link List} of targets.
 */
@FunctionalInterface
public interface TargetLimiter {

    /**
     * Limits a {@link List} of targets to shoot from.
     *
     * @param start   The starting position of the shot
     * @param targets The targets to shoot from
     * @return A modified {@link List} of targets to shoot from
     */
    @NotNull List<Pair<? extends LivingEntity, Vec>> limitTargets(@NotNull Pos start,
                                                                  @NotNull List<Pair<? extends LivingEntity, Vec>> targets);

}
