package org.phantazm.mob.goal;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.target.TargetSelector;
import org.phantazm.mob.validator.TargetValidator;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;
import org.phantazm.proxima.bindings.minestom.goal.ProximaGoal;

import java.util.Objects;
import java.util.Optional;

/**
 * A {@link ProximaGoal} that makes a {@link PhantazmMob} follow {@link Entity}s
 */
@Model("mob.goal.follow_entity")
@Cache(false)
public class FollowEntityGoal implements ProximaGoal {

    private final Data data;
    private final ProximaEntity entity;
    private final TargetSelector<? extends Entity> selector;
    private final TargetValidator validator;
    private Entity target;
    private long ticksSinceTargetChosen;

    /**
     * Creates a {@link FollowEntityGoal}.
     *
     * @param selector The {@link TargetSelector} used to select {@link Entity}s
     */
    @FactoryMethod
    public FollowEntityGoal(@NotNull Data data, @NotNull ProximaEntity entity,
            @NotNull @Child("selector") TargetSelector<? extends Entity> selector,
            @NotNull @Child("validator") TargetValidator validator) {
        this.data = Objects.requireNonNull(data, "data");
        this.entity = Objects.requireNonNull(entity, "entity");
        this.selector = Objects.requireNonNull(selector, "selector");
        this.validator = Objects.requireNonNull(validator, "validator");
        this.ticksSinceTargetChosen = data.retargetInterval();
    }

    @Override
    public boolean shouldStart() {
        return !entity.isDead();
    }

    @Override
    public boolean shouldEnd() {
        return entity.isDead();
    }

    @Override
    public void start() {

    }

    @Override
    public void tick(long time) {
        if ((target != null && target.isRemoved()) || (target != null && !validator.valid(target))) {
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

    @Override
    public void end() {

    }

    private void refreshTarget() {
        ticksSinceTargetChosen = 0L;

        // already check if target removed
        if (target != null && target.getPosition().distanceSquared(entity.getPosition()) <= data.followRange()) {
            return;
        }

        Optional<? extends Entity> newTargetOptional = selector.selectTarget();
        if (newTargetOptional.isPresent()) {
            Entity newTarget = newTargetOptional.get();
            if (validator.valid(newTarget)) {
                entity.setDestination(target = null);
                return;
            }

            entity.setDestination(target = newTarget);
        }
        else {
            entity.setDestination(target = null);
        }
    }

    @DataObject
    public record Data(@NotNull @ChildPath("selector") String selectorPath,
                       @NotNull @ChildPath("validator") String validatorPath,
                       long retargetInterval,
                       double followRange) {

        public Data {
            Objects.requireNonNull(selectorPath, "selectorPath");
        }

    }

}
