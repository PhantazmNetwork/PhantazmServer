package com.github.phantazmnetwork.mob.goal;

import com.github.phantazmnetwork.api.VecUtils;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.target.TargetSelectorInstance;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.NeuralGoal;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class FollowEntityGoal<TEntity extends Entity> implements Goal {

    private final @NotNull TargetSelector<TEntity> selectorCreator;


    public FollowEntityGoal(@NotNull TargetSelector<TEntity> selectorCreator) {
        this.selectorCreator = Objects.requireNonNull(selectorCreator, "selectorCreator");
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
                mob.entity().getNavigator().setDestination(() -> {
                    return selector.selectTarget()
                            .map(player -> VecUtils.toBlockInt(player.getPosition()))
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

    public @NotNull TargetSelector<TEntity> getSelector() {
        return selectorCreator;
    }

}
