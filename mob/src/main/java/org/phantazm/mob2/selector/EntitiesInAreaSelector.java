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
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.TrackerTargetType;
import org.phantazm.mob2.Keys;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.validator.Validator;
import org.phantazm.mob2.validator.ValidatorComponent;

import java.util.ArrayList;
import java.util.List;
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
        return new Internal(injectionStore.get(Keys.MOB_KEY), originSelector.apply(injectionStore),
                validator.apply(injectionStore), data);
    }

    @DataObject
    public record Data(@NotNull @ChildPath("origin") String originSelector,
                       @NotNull @ChildPath("validator") String validator,
                       @NotNull TrackerTargetType target,
                       double range,
                       int limit) {
        @Default("target")
        public static @NotNull ConfigElement targetDefault() {
            return ConfigPrimitive.of("ENTITIES");
        }

        @Default("range")
        public static @NotNull ConfigElement rangeDefault() {
            return ConfigPrimitive.of(-1D);
        }

        @Default("limit")
        public static @NotNull ConfigElement limitDefault() {
            return ConfigPrimitive.of(-1);
        }
    }

    private record Internal(Mob self, Selector originSelector, Validator validator, Data data) implements Selector {
        @Override
        public @NotNull Target select() {
            Instance instance = self.getInstance();
            if (instance == null || data.limit == 0) {
                return Target.NONE;
            }

            Target originTarget = originSelector.select();
            Optional<? extends Point> optionalOrigin = originTarget.location();
            if (optionalOrigin.isEmpty()) {
                return Target.NONE;
            }

            Point origin = optionalOrigin.get();
            List<DoubleObjectPair<Entity>> targets = new ArrayList<>(data.limit < 0 ? 10 : data.limit);

            if (data.range < 0) {
                for (Entity target : instance.getEntityTracker().entities(data.target.target())) {
                    handleEntity(origin, target, targets);
                }
            }
            else {
                instance.getEntityTracker().nearbyEntities(origin, data.range, data.target.target(),
                        target -> handleEntity(origin, target, targets));
            }

            if (targets.isEmpty()) {
                return Target.NONE;
            }

            List<Entity> actualTargets = new ArrayList<>(targets.size());
            for (DoubleObjectPair<Entity> target : targets) {
                actualTargets.add(target.right());
            }

            return Target.entities(actualTargets);
        }

        private void handleEntity(Point origin, Entity target, List<DoubleObjectPair<Entity>> targets) {
            if (!validator.valid(target)) {
                return;
            }

            double thisDistanceSquared = origin.distanceSquared(target.getPosition());

            for (int i = 0; i < targets.size(); i++) {
                DoubleObjectPair<Entity> existingTarget = targets.get(i);
                if (existingTarget.firstDouble() > thisDistanceSquared) {
                    if (targets.size() == data.limit) {
                        targets.remove(targets.size() - 1);
                    }

                    targets.add(i, DoubleObjectPair.of(thisDistanceSquared, target));
                    return;
                }
            }

            if (targets.size() == data.limit) {
                return;
            }

            targets.add(DoubleObjectPair.of(thisDistanceSquared, target));
        }
    }
}
