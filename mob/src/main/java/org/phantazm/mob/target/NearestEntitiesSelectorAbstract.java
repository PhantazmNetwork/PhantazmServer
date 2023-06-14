package org.phantazm.mob.target;

import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.validator.TargetValidator;

import java.util.*;

/**
 * A {@link TargetSelector} that selects nearby {@link Entity}s.
 *
 * @param <TTarget>> A mapped type of the target {@link Entity}
 */
public abstract class NearestEntitiesSelectorAbstract<TTarget extends Entity>
        implements TargetSelector<Iterable<TTarget>> {
    private final double range;
    private final int targetLimit;
    private final TargetValidator targetValidator;

    /**
     * Creates a {@link NearestEntitiesSelectorAbstract}.
     *
     * @param range       The euclidean distance range of the selector
     * @param targetLimit The maximum number of targets to select
     */
    public NearestEntitiesSelectorAbstract(double range, int targetLimit, @NotNull TargetValidator targetValidator) {
        this.range = range;
        this.targetLimit = targetLimit;
        this.targetValidator = Objects.requireNonNull(targetValidator, "targetValidator");
    }

    @Override
    public @NotNull Optional<Iterable<TTarget>> selectTarget(@NotNull PhantazmMob self) {
        Entity entity = self.entity();
        Instance instance = entity.getInstance();
        if (instance == null) {
            return Optional.of(Collections.emptyList());
        }

        Collection<Entity> entities = instance.getNearbyEntities(entity.getPosition(), range);
        Map<UUID, TTarget> targetMap = new HashMap<>(entities.size());
        List<Entity> potentialTargets = new ArrayList<>(entities.size());
        for (Entity nearby : entities) {
            Optional<TTarget> targetOptional = mapTarget(nearby);
            if (targetOptional.isEmpty()) {
                continue;
            }

            TTarget target = targetOptional.get();
            if (!targetValidator.valid(entity, target)) {
                continue;
            }

            targetMap.put(nearby.getUuid(), targetOptional.get());
            potentialTargets.add(nearby);
        }

        potentialTargets.sort(Comparator.comparingDouble(e -> e.getDistanceSquared(entity)));

        int targetCount = Math.min(potentialTargets.size(), targetLimit);
        Collection<TTarget> targets = new ArrayList<>(targetCount);
        for (int i = 0; i < targetCount; i++) {
            targets.add(targetMap.get(potentialTargets.get(i).getUuid()));
        }

        return Optional.of(targets);
    }

    /**
     * Gets the euclidean distance range of the selector.
     *
     * @return The euclidean distance range of the selector
     */
    public double getRange() {
        return range;
    }

    /**
     * Gets the maximum number of targets to select.
     *
     * @return The maximum number of targets to select
     */
    public int getTargetLimit() {
        return targetLimit;
    }

    /**
     * Maps a {@link Entity} to a target.
     *
     * @param entity The {@link Entity} to map
     * @return The mapped target
     */
    protected abstract @NotNull Optional<TTarget> mapTarget(@NotNull Entity entity);
}
