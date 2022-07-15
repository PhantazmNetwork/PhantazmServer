package com.github.phantazmnetwork.zombies.equipment.gun.target.limiter;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * A {@link TargetLimiter} based on distance from the shot's start position.
 */
public class DistanceTargetLimiter implements TargetLimiter {

    private final Data data;

    /**
     * Creates a new {@link DistanceTargetLimiter} with the given {@link Data}.
     *
     * @param data The {@link Data} to use
     */
    public DistanceTargetLimiter(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s
     *
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                int targetLimit = element.getNumberOrThrow("targetLimit").intValue();
                if (targetLimit < 0) {
                    throw new ConfigProcessException("targetLimit must be greater than or equal to 0");
                }
                boolean prioritizeClosest = element.getBooleanOrThrow("prioritizeClosest");

                return new Data(targetLimit, prioritizeClosest);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) {
                ConfigNode node = new LinkedConfigNode(2);
                node.putNumber("targetLimit", data.targetLimit());
                node.putBoolean("prioritizeClosest", data.prioritizeClosest());

                return node;
            }
        };
    }

    @Override
    public @NotNull List<Pair<? extends LivingEntity, Vec>> limitTargets(@NotNull Pos start,
                                                                         @NotNull List<Pair<? extends LivingEntity, Vec>> targets) {
        List<Pair<? extends Entity, Vec>> targetsCopy = new ArrayList<>(targets);
        Comparator<Pair<? extends Entity, Vec>> comparator =
                Comparator.comparingDouble(pair -> start.distanceSquared(pair.value()));
        if (!data.prioritizeClosest()) {
            comparator = comparator.reversed();
        }

        targetsCopy.sort(comparator);
        return targets.subList(0, Math.min(targets.size(), data.targetLimit()));
    }

    /**
     * Data for a {@link DistanceTargetLimiter}.
     *
     * @param targetLimit       The maximum number of targets to select
     * @param prioritizeClosest Whether the closest targets should be prioritized or the farthest targets should be prioritized
     */
    public record Data(int targetLimit, boolean prioritizeClosest) implements Keyed {

        /**
         * The serial {@link Key} of this {@link Data}.
         */
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.target_limiter.distance");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }
}
