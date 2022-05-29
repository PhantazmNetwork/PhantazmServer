package com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal;

import com.github.phantazmnetwork.commons.Tickable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GoalGroup implements Tickable {

    private final Iterable<NeuralGoal> goals;

    private NeuralGoal activeGroup;

    public GoalGroup(@NotNull Iterable<NeuralGoal> goals) {
        this.goals = Objects.requireNonNull(goals, "goals");
    }

    @Override
    public void tick(long time) {
        if (activeGroup == null) {
            chooseGroup();
        }
        else if (activeGroup.shouldEnd()) {
            activeGroup.end();;
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
