package org.phantazm.mob2.goal;

import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.ChildPath;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.proxima.path.PathTarget;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.proxima.bindings.minestom.goal.ProximaGoal;

import java.util.Objects;
import java.util.Optional;

public class FollowEntityGoal implements GoalCreator {
    private final Data data;
    private final Selector selector;

    @FactoryMethod
    public FollowEntityGoal(@NotNull Data data, @NotNull @Child("selector") Selector selector) {
        this.data = Objects.requireNonNull(data);
        this.selector = Objects.requireNonNull(selector);
    }

    @Override
    public @NotNull ProximaGoal create(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Goal(data, selector, mob);
    }

    private static class Goal implements ProximaGoal {
        private final Data data;
        private final Selector selector;
        private final Mob self;

        private Entity target;
        private long ticksSinceTargetChosen;

        private Goal(Data data, Selector selector, Mob self) {
            this.data = Objects.requireNonNull(data);
            this.self = Objects.requireNonNull(self);
            this.selector = Objects.requireNonNull(selector);
            this.ticksSinceTargetChosen = data.retargetInterval();
        }

        @Override
        public boolean shouldStart() {
            return !self.isDead();
        }

        @Override
        public boolean shouldEnd() {
            return self.isDead();
        }

        @Override
        public void tick(long time) {
            if (target != null && target.isRemoved()) {
                target = null;
                refreshTarget();
                return;
            }

            if (ticksSinceTargetChosen >= data.retargetInterval()) {
                refreshTarget();
            } else {
                ++ticksSinceTargetChosen;
            }
        }

        private void refreshTarget() {
            ticksSinceTargetChosen = 0L;

            Target target = selector.select();

            Optional<? extends Entity> newTargetOptional = target.target();
            if (newTargetOptional.isPresent()) {
                Entity newTarget = newTargetOptional.get();
                self.setDestination(newTarget);
                return;
            }

            Optional<? extends Point> optionalPoint = target.location();
            if (optionalPoint.isPresent()) {
                Point point = optionalPoint.get();
                self.setDestination(PathTarget.coordinate(point.blockX(), point.blockY(), point.blockZ()));
                return;
            }

            self.setDestination((PathTarget) null);
            this.target = null;
        }
    }

    @DataObject
    public record Data(@NotNull @ChildPath("selector") String selector, long retargetInterval) {
    }
}
