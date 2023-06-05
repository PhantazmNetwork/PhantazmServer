package org.phantazm.mob.goal;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.target.TargetSelector;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;
import org.phantazm.proxima.bindings.minestom.goal.ProximaGoal;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A {@link ProximaGoal} that makes a {@link PhantazmMob} follow {@link Entity}s
 */
@Model("mob.goal.follow_entity")
@Cache(false)
public class FollowEntityGoal implements GoalCreator {
    private final Data data;
    private final Supplier<TargetSelector<? extends Entity>> selector;

    /**
     * Creates a {@link FollowEntityGoal}.
     *
     * @param selector The {@link TargetSelector} used to select {@link Entity}s
     */
    @FactoryMethod
    public FollowEntityGoal(@NotNull Data data,
            @NotNull @Child("selector") Supplier<TargetSelector<? extends Entity>> selector) {
        this.data = Objects.requireNonNull(data, "data");
        this.selector = Objects.requireNonNull(selector, "selector");
    }

    @Override
    public @NotNull ProximaGoal create(@NotNull PhantazmMob mob) {
        return new Goal(data, selector.get(), mob);
    }

    private static class Goal implements ProximaGoal {
        private final Data data;
        private final TargetSelector<? extends Entity> selector;
        private final PhantazmMob self;

        private Entity target;
        private long ticksSinceTargetChosen;

        private Goal(Data data, TargetSelector<? extends Entity> selector, PhantazmMob self) {
            this.data = Objects.requireNonNull(data, "data");
            this.self = Objects.requireNonNull(self, "self");
            this.selector = Objects.requireNonNull(selector, "selector");
            this.ticksSinceTargetChosen = data.retargetInterval();
        }

        @Override
        public boolean shouldStart() {
            return !self.entity().isDead();
        }

        @Override
        public boolean shouldEnd() {
            return self.entity().isDead();
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
            }
            else {
                ++ticksSinceTargetChosen;
            }
        }

        private void refreshTarget() {
            ticksSinceTargetChosen = 0L;
            ProximaEntity entity = self.entity();

            Optional<? extends Entity> newTargetOptional = selector.selectTarget(self);
            if (newTargetOptional.isPresent()) {
                Entity newTarget = newTargetOptional.get();
                entity.setDestination(target = newTarget);
                return;
            }

            entity.setDestination(target = null);
        }
    }

    @DataObject
    public record Data(@NotNull @ChildPath("selector") String selector, long retargetInterval) {

        public Data {
            Objects.requireNonNull(selector, "selector");
        }

    }

}
