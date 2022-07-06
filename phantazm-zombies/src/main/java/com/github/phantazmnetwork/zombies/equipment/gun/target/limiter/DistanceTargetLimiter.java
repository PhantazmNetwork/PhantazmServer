package com.github.phantazmnetwork.zombies.equipment.gun.target.limiter;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.ToDoubleFunction;

public class DistanceTargetLimiter implements TargetLimiter {

    public record Data(int targetLimit, boolean prioritizeClosest) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM,"gun.target_limiter.distance");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                int targetLimit = element.getNumberOrThrow("targetLimit").intValue();
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

    private final Data data;

    public DistanceTargetLimiter(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull List<Pair<? extends LivingEntity, Vec>> limitTargets(@NotNull Pos start,
                                                                         @NotNull List<Pair<? extends LivingEntity, Vec>> targets) {
        List<Pair<? extends Entity, Vec>> targetsCopy = new ArrayList<>(targets);
        Comparator<Pair<? extends Entity, Vec>> comparator = Comparator.comparingDouble(new ToDoubleFunction<>() {

            private final Object2DoubleMap<UUID> distanceMap = new Object2DoubleOpenHashMap<>(targets.size());

            @Override
            public double applyAsDouble(Pair<? extends Entity, Vec> value) {
                return distanceMap.computeIfAbsent(value.left().getUuid(),
                        unused -> value.right().distanceSquared(start));
            }
        });
        if (!data.prioritizeClosest()) {
            comparator = comparator.reversed();
        }

        targetsCopy.sort(comparator);
        return targets.subList(0, Math.min(targets.size(), data.targetLimit()));
    }
}
