package com.github.phantazmnetwork.mob.goal;

import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.NeuralGoal;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link NeuralGoal} that makes a {@link PhantazmMob} follow {@link Player}s.
 */
@Model("mob.goal.follow_player")
public class FollowPlayerGoal extends FollowEntityGoal<Player> {

    @DataObject
    public record Data(@NotNull @DataPath("target_selector") String targetSelectorKey) {

        public Data {
            Objects.requireNonNull(targetSelectorKey, "targetSelectorKey");
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

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                String targetSelectorKey = element.getStringOrThrow("targetSelectorKey");
                return new Data(targetSelectorKey);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) {
                return ConfigNode.of("targetSelectorKey", data.targetSelectorKey());
            }
        };
    }

}
