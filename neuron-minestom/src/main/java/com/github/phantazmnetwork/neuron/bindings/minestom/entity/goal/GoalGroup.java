package com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

/**
 * Represents a group of {@link NeuralGoal}s.
 * Only one {@link NeuralGoal} can be active at a time in a group.
 */
@Model("neuron.goal.group")
public class GoalGroup implements Tickable {

    @DataObject
    public record Data(@NotNull @DataPath("goals") Collection<String> goalPaths) {

        public Data {
            Objects.requireNonNull(goalPaths, "goalPaths");
        }

    }

    private final Iterable<NeuralGoal> goals;

    private NeuralGoal activeGroup;

    /**
     * Creates a {@link GoalGroup}.
     *
     * @param goals The {@link NeuralGoal}s in the group
     */
    @FactoryMethod
    public GoalGroup(@NotNull Data data, @NotNull @DataName("goals") Collection<NeuralGoal> goals) {
        this(goals);
    }

    public GoalGroup(@NotNull Collection<NeuralGoal> goals) {
        this.goals = Objects.requireNonNull(goals, "goals");
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Collection<String>> pathProcessor = ConfigProcessor.STRING.collectionProcessor();
        return new ConfigProcessor<Data>() {
            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Collection<String> goalPaths = pathProcessor.dataFromElement(element.getElementOrThrow("goalPaths"));
                return new Data(goalPaths);
            }

            @Override
            public @NotNull ConfigElement elementFromData(Data data) throws ConfigProcessException {
                return ConfigNode.of("goalPaths", pathProcessor.elementFromData(data.goalPaths()));
            }
        };
    }

    @Override
    public void tick(long time) {
        if (activeGroup == null) {
            chooseGroup();
        }
        else if (activeGroup.shouldEnd()) {
            activeGroup.end();
            activeGroup = null;
        }
        else {
            activeGroup.tick(time);
        }
    }

    private void chooseGroup() {
        for (NeuralGoal goal : goals) {
            if (goal.shouldStart()) {
                activeGroup = goal;
                goal.start();
                break;
            }
        }
    }

}
