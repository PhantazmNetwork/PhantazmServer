package com.github.phantazmnetwork.mob.goal;

import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.MinestomDescriptor;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.NeuralGoal;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link NeuralGoal} that makes a {@link PhantazmMob} follow {@link Entity}s
 *
 * @param <TEntity> The type of {@link Entity} to follow
 */
public abstract class FollowEntityGoal<TEntity extends Entity> implements NeuralGoal {

    private final NeuralEntity entity;

    private final TargetSelector<TEntity> selector;

    private final long retargetInterval;

    private long ticksSinceTargetChosen;

    /**
     * Creates a {@link FollowEntityGoal}.
     *
     * @param selector The {@link TargetSelector} used to select {@link Entity}s
     */
    public FollowEntityGoal(@NotNull NeuralEntity entity, @NotNull TargetSelector<TEntity> selector,
            long retargetInterval) {
        this.entity = Objects.requireNonNull(entity, "entity");
        this.selector = Objects.requireNonNull(selector, "selector");
        this.retargetInterval = retargetInterval;
        this.ticksSinceTargetChosen = retargetInterval;
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
        if (ticksSinceTargetChosen >= retargetInterval) {
            refreshTarget();
        }
        else {
            ++ticksSinceTargetChosen;
        }
    }

    private void refreshTarget() {
        MinestomDescriptor descriptor = (MinestomDescriptor)entity.getDescriptor();
        entity.getNavigator()
                .setDestination(() -> selector.selectTarget().map(descriptor::computeTargetPosition).orElse(null));
    }

    /**
     * Gets the {@link TargetSelector} used to select {@link Entity}s.
     *
     * @return The {@link TargetSelector} used to select {@link Entity}s
     */
    public @NotNull TargetSelector<TEntity> getSelector() {
        return selector;
    }

}
