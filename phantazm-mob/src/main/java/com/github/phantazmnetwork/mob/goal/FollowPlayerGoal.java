package com.github.phantazmnetwork.mob.goal;

import com.github.phantazmnetwork.api.VecUtils;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.NeuralGoal;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class FollowPlayerGoal implements NeuralGoal {

    private final PhantazmMob mob;

    private final TargetSelector<Player> playerSelector;

    public FollowPlayerGoal(@NotNull PhantazmMob mob, @NotNull TargetSelector<Player> playerSelector) {
        this.mob = Objects.requireNonNull(mob, "mob");
        this.playerSelector = Objects.requireNonNull(playerSelector, "playerSelector");
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
            return playerSelector.selectTarget(mob)
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
