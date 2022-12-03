package com.github.phantazmnetwork.mob.goal;

import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.NeuralGoal;
import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * A {@link NeuralGoal} that makes a {@link PhantazmMob} follow {@link Entity}s
 */
@Model("mob.goal.follow_entity")
public class FollowEntityGoal implements NeuralGoal {

    private final Data data;
    private final NeuralEntity entity;
    private final TargetSelector<? extends Entity> selector;
    private Entity target;
    private long ticksSinceTargetChosen;

    /**
     * Creates a {@link FollowEntityGoal}.
     *
     * @param selector The {@link TargetSelector} used to select {@link Entity}s
     */
    @FactoryMethod
    public FollowEntityGoal(@NotNull Data data, @NotNull @Dependency("mob.entity.neural_entity") NeuralEntity entity,
            @NotNull @DataName("selector") TargetSelector<? extends Entity> selector) {
        this.data = Objects.requireNonNull(data, "data");
        this.entity = Objects.requireNonNull(entity, "entity");
        this.selector = Objects.requireNonNull(selector, "selector");
        this.ticksSinceTargetChosen = data.retargetInterval();
    }

    @Override
    public boolean shouldStart() {
        return true;
    }

    @Override
    public boolean shouldEnd() {
        return false;
    }

    @Override
    public void start() {

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
            entity.setTarget(target = newTargetOptional.get());
        }
        else {
            entity.setTarget(target = null);
        }
    }

    @DataObject
    public record Data(@NotNull @DataPath("selector") String selectorPath, long retargetInterval, double followRange) {

        public Data {
            Objects.requireNonNull(selectorPath, "selectorPath");
        }

    }

}
