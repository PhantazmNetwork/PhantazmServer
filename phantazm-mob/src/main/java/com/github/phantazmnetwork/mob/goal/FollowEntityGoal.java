package com.github.phantazmnetwork.mob.goal;

import com.github.phantazmnetwork.api.VecUtils;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.NeuralGoal;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class FollowEntityGoal<TEntity extends Entity> implements NeuralGoal {

    private final PhantazmMob mob;

    private final TargetSelector<TEntity> entitySelector;

    public FollowEntityGoal(@NotNull PhantazmMob mob, @NotNull TargetSelector<TEntity> playerSelector) {
        this.mob = Objects.requireNonNull(mob, "mob");
        this.entitySelector = Objects.requireNonNull(playerSelector, "entitySelector");
    }

    public @NotNull TargetSelector<TEntity> getEntitySelector() {
        return entitySelector;
    }

    @Override
    public void tick(long time) {

    }

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
}
