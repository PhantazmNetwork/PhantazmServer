package org.phantazm.mob.goal;

import org.jetbrains.annotations.NotNull;
import org.phantazm.proxima.bindings.minestom.goal.GoalGroup;
import org.phantazm.proxima.bindings.minestom.goal.ProximaGoal;

import java.util.Collection;
import java.util.Optional;

public class CollectionGoalGroup implements GoalGroup {
    private final ProximaGoal[] goals;

    private ProximaGoal activeGoal;
    private int activeGoalIndex;

    public CollectionGoalGroup(@NotNull Collection<ProximaGoal> goals) {
        this.goals = goals.toArray(ProximaGoal[]::new);
    }

    @Override
    public void tick(long time) {
        if (activeGoal == null) {
            for (int i = 0; i < goals.length; i++) {
                ProximaGoal goal = goals[i];
                if (goal.shouldStart()) {
                    goal.start();
                    activeGoal = goal;
                    activeGoalIndex = i;
                    break;
                }
            }

            return;
        }

        activeGoal.tick(time);

        int nextIndex = activeGoalIndex + 1;
        if (nextIndex < goals.length) {
            ProximaGoal nextGoal = goals[nextIndex];
            if (nextGoal.shouldStart()) {
                activeGoal.end();

                nextGoal.start();
                activeGoal = nextGoal;
                activeGoalIndex = nextIndex;
                return;
            }
        }

        if (activeGoal.shouldEnd()) {
            activeGoal.end();
            for (int i = activeGoalIndex - 1; i >= 0 && i < goals.length; i--) {
                ProximaGoal goal = goals[i];
                if (goal.shouldStart()) {
                    goal.start();
                    activeGoal = goal;
                    activeGoalIndex = i;
                    break;
                }
            }
        }
    }

    @Override
    public @NotNull Optional<ProximaGoal> currentGoal() {
        return Optional.ofNullable(activeGoal);
    }
}
