package com.github.phantazmnetwork.mob.target;

import com.github.phantazmnetwork.api.target.TargetSelectorInstance;
import com.github.phantazmnetwork.mob.PhantazmMob;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A {@link TargetSelector} that selects nearby {@link Entity}s.
 * @param <TReturn> A mapped type of the target {@link Entity}
 */
public abstract class NearestEntitiesSelector<TReturn> implements TargetSelector<Iterable<TReturn>> {

    private final double range;

    private final int targetLimit;

    /**
     * Creates a {@link NearestEntitiesSelector}.
     * @param range The range of the selector
     * @param targetLimit The maximum number of targets to select
     */
    public NearestEntitiesSelector(double range, int targetLimit) {
        this.range = range;
        this.targetLimit = targetLimit;
    }

    @Override
    public @NotNull TargetSelectorInstance<Iterable<TReturn>> createSelector(@NotNull PhantazmMob mob) {
        return () -> {
            Instance instance = mob.entity().getInstance();
            if (instance == null) {
                throw new IllegalStateException("Instance unset");
            }

            Collection<Entity> entities = instance.getNearbyEntities(mob.entity().getPosition(), range);
            Map<UUID, TReturn> targetMap = new HashMap<>(entities.size());
            List<Entity> potentialTargets = new ArrayList<>(entities.size());
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

            potentialTargets.sort(Comparator.comparingDouble(entity -> mob.entity().getDistanceSquared(entity)));

            int targetCount = Math.min(potentialTargets.size(), targetLimit);
            Collection<TReturn> targets = new ArrayList<>(targetCount);
            for (int i = 0; i < targetCount; i++) {
                targets.add(targetMap.get(potentialTargets.get(i).getUuid()));
            }

            return Optional.of(targets);
        };
    }

    /**
     * Gets the range of the selector.
     * @return The range of the selector
     */
    public double getRange() {
        return range;
    }

    /**
     * Gets the maximum number of targets to select.
     * @return The maximum number of targets to select
     */
    public int getTargetLimit() {
        return targetLimit;
    }

    /**
     * Maps a {@link Entity} to a target.
     * @param entity The {@link Entity} to map
     * @return The mapped target
     */
    protected abstract @NotNull Optional<TReturn> mapTarget(@NotNull Entity entity);

    /**
     * Checks if a target is valid.
     * @param mob The mob using the {@link NearestPlayersSelector}
     * @param targetEntity The target {@link Entity}
     * @param target The mapped target
     * @return Whether the target is valid
     */
    protected abstract boolean isTargetValid(@NotNull PhantazmMob mob, @NotNull Entity targetEntity,
                                             @NotNull TReturn target);

}
