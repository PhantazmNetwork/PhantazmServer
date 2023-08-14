package org.phantazm.mob2.selector;

import it.unimi.dsi.fastutil.doubles.DoubleObjectPair;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.validator.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class AreaSelectorAbstract<T extends Entity> implements Selector {
    private final Mob self;
    private final Selector originSelector;
    private final EntityTracker.Target<? extends Entity> target;
    private final Validator validator;

    private final int limit;
    private final double range;

    public <V extends Entity> AreaSelectorAbstract(Mob self, Selector originSelector, EntityTracker.Target<V> target,
            Validator validator, int limit, double range) {
        this.self = Objects.requireNonNull(self, "self");
        this.originSelector = Objects.requireNonNull(originSelector, "originSelector");
        this.target = Objects.requireNonNull(target, "target");
        this.validator = Objects.requireNonNull(validator, "validator");

        this.limit = limit;
        this.range = range;
    }

    @Override
    public @NotNull Target select() {
        Instance instance = self.getInstance();
        if (instance == null || limit == 0) {
            return Target.NONE;
        }

        Target originTarget = originSelector.select();
        Optional<? extends Point> optionalPoint = originTarget.location();
        if (optionalPoint.isEmpty()) {
            return Target.NONE;
        }

        Point point = optionalPoint.get();
        boolean unlimited = limit < 0;
        List<DoubleObjectPair<T>> targets = new ArrayList<>(unlimited ? 10 : limit);

        instance.getEntityTracker().nearbyEntities(point, range, target, targetEntity -> {
            T target = mapEntity(targetEntity);
            if (target == null || !validator.valid(targetEntity)) {
                return;
            }

            double thisDistanceSquared = point.distanceSquared(target.getPosition());

            for (int i = 0; i < targets.size(); i++) {
                DoubleObjectPair<T> existingTargets = targets.get(i);
                if (existingTargets.firstDouble() > thisDistanceSquared) {
                    if (!unlimited && targets.size() == limit) {
                        targets.remove(targets.size() - 1);
                    }

                    targets.add(i, DoubleObjectPair.of(thisDistanceSquared, target));
                    return;
                }
            }

            if (!unlimited && targets.size() == limit) {
                return;
            }

            targets.add(DoubleObjectPair.of(thisDistanceSquared, target));
        });

        if (targets.isEmpty()) {
            return Target.NONE;
        }

        List<T> players = new ArrayList<>(targets.size());
        for (DoubleObjectPair<T> target : targets) {
            players.add(target.right());
        }

        return Target.entities(players);
    }

    protected abstract @Nullable T mapEntity(@NotNull Entity entity);
}
