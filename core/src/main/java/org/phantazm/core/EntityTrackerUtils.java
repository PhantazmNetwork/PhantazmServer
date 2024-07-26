package org.phantazm.core;

import it.unimi.dsi.fastutil.doubles.DoubleObjectPair;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Utils for locating and processing/targeting entities.
 */
public final class EntityTrackerUtils {
    private EntityTrackerUtils() {
    }

    /**
     * Produces a {@link Target} from nearby entities in an instance.
     *
     * @param instance     the instance from which to select entities
     * @param targetType   the {@link EntityTracker.Target}, which indicates which kind of entities to search for
     * @param originTarget the origin point(s) from which to measure distance
     * @param limit        the maximum number of entities to select; if less than 0, there are no limits
     * @param range        the distance within which to search for entities; if less than 0, selects all entities in the
     *                     range
     * @param entityTester a predicate used to filter entities; only entities for which this predicate returns true will
     *                     be present in the final target
     * @param <T>          the entity type
     * @return a new Target instance
     */
    public static <T extends Entity> @NotNull Target select(@NotNull Instance instance,
        @NotNull EntityTracker.Target<T> targetType, @NotNull Target originTarget, int limit, double range,
        @NotNull Predicate<? super T> entityTester) {
        Collection<? extends Point> origins = originTarget.locations();
        if (origins.isEmpty()) {
            return Target.NONE;
        }

        List<DoubleObjectPair<T>> targets = new ArrayList<>(limit < 0 ? 10 : limit);
        for (Point origin : origins) {
            if (range < 0) {
                for (T target : instance.getEntityTracker().entities(targetType)) {
                    handleEntity(origin, target, targets, entityTester, limit);
                }
            } else {
                instance.getEntityTracker().nearbyEntities(origin, range, targetType,
                    target -> handleEntity(origin, target, targets, entityTester, limit));
            }
        }

        if (targets.isEmpty()) {
            return Target.NONE;
        }

        List<T> entities = new ArrayList<>(targets.size());
        for (DoubleObjectPair<T> pair : targets) {
            entities.add(pair.right());
        }

        return Target.entities(entities);
    }

    private static <T extends Entity> void handleEntity(Point origin, T target, List<DoubleObjectPair<T>> targets,
        Predicate<? super T> predicate, int limit) {
        if (!predicate.test(target)) {
            return;
        }

        double thisDistanceSquared = origin.distanceSquared(target.getPosition());

        for (int i = 0; i < targets.size(); i++) {
            DoubleObjectPair<T> existingTarget = targets.get(i);
            if (existingTarget.firstDouble() > thisDistanceSquared) {
                if (targets.size() == limit) {
                    targets.remove(targets.size() - 1);
                }

                targets.add(i, DoubleObjectPair.of(thisDistanceSquared, target));
                return;
            }
        }

        if (targets.size() == limit) {
            return;
        }

        targets.add(DoubleObjectPair.of(thisDistanceSquared, target));
    }
}
