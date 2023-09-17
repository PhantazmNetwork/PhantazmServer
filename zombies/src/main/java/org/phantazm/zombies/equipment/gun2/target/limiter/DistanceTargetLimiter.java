package org.phantazm.zombies.equipment.gun2.target.limiter;

import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.PlayerComponent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class DistanceTargetLimiter implements PlayerComponent<TargetLimiter> {

    private final Data data;

    public DistanceTargetLimiter(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull TargetLimiter forPlayer(@NotNull ZombiesPlayer player, @NotNull InjectionStore injectionStore) {
        return new Limiter(data.prioritizeClosest(), data.targetLimit());
    }

    private static class Limiter implements TargetLimiter {

        private final boolean prioritizeClosest;

        private final int targetLimit;

        public Limiter(boolean prioritizeClosest, int targetLimit) {
            this.prioritizeClosest = prioritizeClosest;
            this.targetLimit = targetLimit;
        }

        @Override
        public @NotNull List<Pair<? extends LivingEntity, Vec>> limitTargets(@NotNull Pos start, @NotNull List<Pair<? extends LivingEntity, Vec>> targets) {
            List<Pair<? extends LivingEntity, Vec>> targetsCopy = new ArrayList<>(targets);
            Comparator<Pair<? extends LivingEntity, Vec>> comparator =
                Comparator.comparingDouble(pair -> start.distanceSquared(pair.value()));
            if (!prioritizeClosest) {
                comparator = comparator.reversed();
            }

            targetsCopy.sort(comparator);
            return targetsCopy.subList(0, Math.min(targets.size(), targetLimit));
        }
    }

    public record Data(int targetLimit,
        boolean prioritizeClosest) {
    }
}
