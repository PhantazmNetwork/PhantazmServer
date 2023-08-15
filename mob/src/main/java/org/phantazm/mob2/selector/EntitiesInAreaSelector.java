package org.phantazm.mob2.selector;

import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.ChildPath;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import it.unimi.dsi.fastutil.doubles.DoubleObjectPair;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Keys;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.validator.Validator;
import org.phantazm.mob2.validator.ValidatorComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class EntitiesInAreaSelector implements SelectorComponent {
    private final Data data;
    private final SelectorComponent originSelector;
    private final ValidatorComponent validator;

    @FactoryMethod
    public EntitiesInAreaSelector(@NotNull Data data, @NotNull @Child("origin") SelectorComponent originSelector,
            @NotNull @Child("validator") ValidatorComponent validator) {
        this.data = data;
        this.originSelector = originSelector;
        this.validator = validator;
    }

    @Override
    public @NotNull Selector apply(@NotNull InjectionStore injectionStore) {
        EntityTracker.Target<?> target = switch (data.target) {
            case PLAYERS -> EntityTracker.Target.PLAYERS;
            case ENTITIES -> EntityTracker.Target.LIVING_ENTITIES;
        };

        return new Internal<>(injectionStore.get(Keys.MOB_KEY), originSelector.apply(injectionStore), target,
                validator.apply(injectionStore), data.limit, data.range, data.excludeSelf);
    }

    public enum TargetType {
        PLAYERS,
        ENTITIES
    }

    @DataObject
    public record Data(@NotNull @ChildPath("origin") String originSelector,
                       @NotNull @ChildPath("validator") String validator,
                       @NotNull TargetType target,
                       double range,
                       int limit,
                       boolean excludeSelf) {
        @Default("target")
        public static @NotNull ConfigElement targetDefault() {
            return ConfigPrimitive.of("ENTITIES");
        }

        @Default("limit")
        public static @NotNull ConfigElement limitDefault() {
            return ConfigPrimitive.of(-1);
        }

        @Default("excludeSelf")
        public static @NotNull ConfigElement excludeSelfDefault() {
            return ConfigPrimitive.of(true);
        }
    }

    private record Internal<T extends Entity>(Mob self,
                                              Selector originSelector,
                                              EntityTracker.Target<T> target,
                                              Validator validator,
                                              int limit,
                                              double range,
                                              boolean excludeSelf) implements Selector {
        private Internal(Mob self, Selector originSelector, EntityTracker.Target<T> target, Validator validator,
                int limit, double range, boolean excludeSelf) {
            this.self = Objects.requireNonNull(self, "self");
            this.originSelector = Objects.requireNonNull(originSelector, "originSelector");
            this.target = Objects.requireNonNull(target, "target");
            this.validator = Objects.requireNonNull(validator, "validator");

            this.limit = limit;
            this.range = range;
            this.excludeSelf = excludeSelf;
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
            List<DoubleObjectPair<T>> targets = new ArrayList<>(limit < 0 ? 10 : limit);

            instance.getEntityTracker().nearbyEntities(point, range, target, target -> {
                if ((target == self && excludeSelf) || !validator.valid(target)) {
                    return;
                }

                double thisDistanceSquared = point.distanceSquared(target.getPosition());

                for (int i = 0; i < targets.size(); i++) {
                    DoubleObjectPair<T> existingTargets = targets.get(i);
                    if (existingTargets.firstDouble() > thisDistanceSquared) {
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
            });

            if (targets.isEmpty()) {
                return Target.NONE;
            }

            List<T> actualTargets = new ArrayList<>(targets.size());
            for (DoubleObjectPair<T> target : targets) {
                actualTargets.add(target.right());
            }

            return Target.entities(actualTargets);
        }
    }
}
