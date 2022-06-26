package com.github.phantazmnetwork.mob.goal;

import com.github.phantazmnetwork.api.VecUtils;
import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.NeuralGoal;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record FollowEntityGoal<TEntity extends Entity>(@NotNull TargetSelector<TEntity> entitySelector) implements GoalCreator, VariantSerializable {

    public final static String SERIAL_NAME = "followEntityGoal";

    public FollowEntityGoal(@NotNull TargetSelector<TEntity> entitySelector) {
        this.entitySelector = Objects.requireNonNull(entitySelector, "entitySelector");
    }

    @Override
    public @NotNull NeuralGoal createGoal(@NotNull PhantazmMob mob) {
        return new NeuralGoal() {
            @Override
            public boolean shouldStart() {
                return true;
            }

            @Override
            public void start() {
                mob.entity().getNavigator().setDestination(() -> {
                    return entitySelector.selectTarget(mob)
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

    @Override
    public @NotNull String getSerialName() {
        return SERIAL_NAME;
    }
}
