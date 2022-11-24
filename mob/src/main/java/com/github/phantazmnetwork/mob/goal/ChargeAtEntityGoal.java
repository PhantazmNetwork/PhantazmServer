package com.github.phantazmnetwork.mob.goal;

import com.github.phantazmnetwork.mob.target.TargetSelector;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.NeuralGoal;
import com.github.steanky.element.core.annotation.*;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

@Model("mob.goal.charge_at_entity")
public class ChargeAtEntityGoal implements NeuralGoal {

    @DataObject
    public record Data(@NotNull @DataPath("selector") String selectorPath,
                       long retargetInterval,
                       double followRange,
                       long chargeInterval,
                       double chargeSpeed) {

        public Data {
            Objects.requireNonNull(selectorPath, "selectorPath");
        }

    }

    private final Data data;

    private final NeuralEntity entity;

    private final TargetSelector<? extends Entity> selector;

    private Entity target;

    private long ticksSinceTargetChosen;

    private long ticksSinceCharge;

    /**
     * Creates a {@link FollowEntityGoal}.
     *
     * @param selector The {@link TargetSelector} used to select {@link Entity}s
     */
    @FactoryMethod
    public ChargeAtEntityGoal(@NotNull Data data, @NotNull @Dependency("mob.entity.neural_entity") NeuralEntity entity,
            @NotNull @DataName("selector") TargetSelector<? extends Entity> selector) {
        this.data = Objects.requireNonNull(data, "data");
        this.entity = Objects.requireNonNull(entity, "entity");
        this.selector = Objects.requireNonNull(selector, "selector");
        this.ticksSinceTargetChosen = data.retargetInterval();
        this.ticksSinceCharge = data.chargeInterval();
    }

    @Override
    public boolean shouldStart() {
        return true;
    }

    @Override
    public void start() {

    }

    @Override
    public boolean shouldEnd() {
        return false;
    }

    @Override
    public void end() {

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

        if (ticksSinceCharge >= data.chargeInterval()) {
            charge();
        }
        else {
            ++ticksSinceCharge;
        }
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
            ticksSinceCharge = 0L;
        }
        else {
            entity.setTarget(target = null);
            ticksSinceCharge = data.chargeInterval();
        }
    }

    private void charge() {
        Vec direction =
                Vec.fromPoint(target.getPosition().sub(entity.getPosition())).normalize().mul(data.chargeSpeed());
        entity.setVelocity(entity.getVelocity().add(direction));

        ticksSinceCharge = 0L;
    }

}
