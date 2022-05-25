package com.github.phantazmnetwork.mob.target;

import com.github.phantazmnetwork.mob.PhantazmMob;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.ToDoubleFunction;

public abstract class NearestEntitySelector<TReturn> implements TargetSelector<Iterable<TReturn>> {

    private final double range;

    private final int limit;

    public NearestEntitySelector(double range, int limit) {
        this.range = range;
        this.limit = limit;
    }

    @Override
    public @NotNull Optional<Iterable<TReturn>> selectTarget(@NotNull PhantazmMob<?> mob) {
        Instance instance = mob.entity().getInstance();
        if (instance == null) {
            throw new IllegalStateException("instance unset");
        }

        Iterable<Entity> entities = instance.getNearbyEntities(mob.entity().getPosition(), range);
        Map<UUID, TReturn> targetMap = new HashMap<>();
        List<Entity> potentialTargets = new ArrayList<>();
        for (Entity entity : entities) {
            Optional<TReturn> targetOptional = mapTarget(entity);
            if (targetOptional.isEmpty()) {
                continue;
            }

            TReturn target = targetOptional.get();
            if (!isTargetValid(mob, entity, target)) {
                continue;
            }

            targetMap.put(entity.getUuid(), targetOptional.get());
            potentialTargets.add(entity);
        }

        potentialTargets.sort(Comparator.comparingDouble(new ToDoubleFunction<>() {

            private final Object2DoubleMap<UUID> distanceMap = new Object2DoubleOpenHashMap<>(potentialTargets.size());

            @Override
            public double applyAsDouble(Entity value) {
                return distanceMap.computeIfAbsent(value.getUuid(), (unused) -> mob.entity().getDistanceSquared(value));
            }
        }));

        int targetCount = Math.min(potentialTargets.size(), limit);
        Collection<TReturn> targets = new ArrayList<>(targetCount);
        for (int i = 0; i < targetCount; i++) {
            targets.add(targetMap.get(potentialTargets.get(i).getUuid()));
        }

        return Optional.of(targets);
    }

    protected abstract @NotNull Optional<TReturn> mapTarget(@NotNull Entity entity);

    protected abstract boolean isTargetValid(@NotNull PhantazmMob<?> mob, @NotNull Entity targetEntity,
                                             @NotNull TReturn target);

}
