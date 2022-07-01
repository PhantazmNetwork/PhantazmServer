package com.github.phantazmnetwork.mob.goal;

import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import com.github.phantazmnetwork.api.target.TargetSelectorInstance;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.MinestomDescriptor;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.NeuralGoal;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link Goal} that makes a {@link PhantazmMob} follow {@link Entity}s
 * @param <TEntity> The type of {@link Entity} to follow
 */
public abstract class FollowEntityGoal<TEntity extends Entity> implements Goal {

    private final @NotNull TargetSelector<TEntity> selectorCreator;

    /**
     * Creates a {@link FollowEntityGoal}.
     * @param selector The {@link TargetSelector} used to select {@link Entity}s
     */
    public FollowEntityGoal(@NotNull TargetSelector<TEntity> selector) {
        this.selectorCreator = Objects.requireNonNull(selector, "selector");
    }

    @Override
    public @NotNull NeuralGoal createGoal(@NotNull PhantazmMob mob) {
        TargetSelectorInstance<TEntity> selector = selectorCreator.createSelector(mob);

        return new NeuralGoal() {
            @Override
            public boolean shouldStart() {
                return true;
            }

            @Override
            public void start() {
                MinestomDescriptor descriptor = (MinestomDescriptor) mob.entity().getDescriptor();
                mob.entity().getNavigator().setDestination(() -> {
                    return selector.selectTarget()
                            .map(descriptor::computeTargetPosition)
                            .orElse(null);
                });
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

            }
        };
    }

    /**
     * Gets the {@link TargetSelector} used to select {@link Entity}s.
     * @return The {@link TargetSelector} used to select {@link Entity}s
     */
    public @NotNull TargetSelector<TEntity> getSelector() {
        return selectorCreator;
    }

}
