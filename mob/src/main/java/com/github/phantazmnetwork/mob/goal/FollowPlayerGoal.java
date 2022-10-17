package com.github.phantazmnetwork.mob.goal;

import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.NeuralGoal;
import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link NeuralGoal} that makes a {@link PhantazmMob} follow {@link Player}s.
 */
@Model("mob.goal.follow_player")
public class FollowPlayerGoal extends FollowEntityGoal<Player> {

    @DataObject
    public record Data(@NotNull @DataPath("target_selector") String targetSelectorPath) {

        public Data {
            Objects.requireNonNull(targetSelectorPath, "targetSelectorPath");
        }

    }

    /**
     * Creates a new {@link FollowPlayerGoal}.
     *
     * @param selector The {@link TargetSelector} used to select {@link Player}s
     */
    @FactoryMethod
    public FollowPlayerGoal(@NotNull Data data, @NotNull @Dependency("mob.entity.neural") NeuralEntity entity,
            @NotNull @DataName("target_selector") TargetSelector<Player> selector) {
        super(entity, selector);
    }

}
