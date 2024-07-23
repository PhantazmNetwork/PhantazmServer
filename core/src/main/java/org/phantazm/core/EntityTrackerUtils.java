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

public final class EntityTrackerUtils {
    private EntityTrackerUtils() {
    }

    public static @NotNull Target select(@NotNull Instance instance, @NotNull EntityTracker.Target<?> targetType,
        @NotNull Target originTarget, int limit, double range, @NotNull Predicate<? super Entity> entityTester) {
        Collection<? extends Point> origins = originTarget.locations();
        if (origins.isEmpty()) {
            return Target.NONE;
        }

        List<Entity> actualTargets = null;
        for (Point origin : originTarget.locations()) {
            List<DoubleObjectPair<Entity>> targets = new ArrayList<>(limit < 0 ? 10 : limit);

            if (range < 0) {
                for (Entity target : instance.getEntityTracker().entities(targetType)) {
                    handleEntity(origin, target, targets, entityTester, limit);
                }
            } else {
                instance.getEntityTracker().nearbyEntities(origin, range, targetType,
                    target -> handleEntity(origin, target, targets, entityTester, limit));
            }

            if (targets.isEmpty()) {
                continue;
            }

            if (actualTargets == null) {
                actualTargets = new ArrayList<>();
            }

            for (DoubleObjectPair<Entity> target : targets) {
                actualTargets.add(target.right());
            }
        }

        return actualTargets == null ? Target.NONE : Target.entities(actualTargets);
    }

    private static void handleEntity(Point origin, Entity target, List<DoubleObjectPair<Entity>> targets,
        Predicate<? super Entity> predicate, int limit) {
        if (!predicate.test(target)) {
            return;
        }

        double thisDistanceSquared = origin.distanceSquared(target.getPosition());

        for (int i = 0; i < targets.size(); i++) {
            DoubleObjectPair<Entity> existingTarget = targets.get(i);
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
