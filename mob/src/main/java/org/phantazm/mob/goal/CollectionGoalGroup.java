package org.phantazm.mob.goal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.proxima.bindings.minestom.goal.GoalGroup;
import org.phantazm.proxima.bindings.minestom.goal.ProximaGoal;

import java.util.Collection;

public class CollectionGoalGroup implements GoalGroup {
    private final ProximaGoal[] goals;

    private ProximaGoal activeGoal;
    private int activeGoalIndex;

    public CollectionGoalGroup(@NotNull Collection<ProximaGoal> goals) {
        this.goals = goals.toArray(ProximaGoal[]::new);
        this.activeGoalIndex = 0;
    }

    @Override
    public void tick(long time) {
        if (activeGoal == null) {
            ProximaGoal target = null;
            int targetIndex = 0;

            for (int i = 0; i < goals.length; i++) {
                ProximaGoal goal = goals[i];

                if (!goal.shouldStart()) {
                    break;
                }

                target = goal;
                targetIndex = i;
            }

            if (target != null) {
                activeGoal = target;
                activeGoalIndex = targetIndex;

                target.start();
            }

            return;
        }

        ProximaGoal target = null;
        int targetIndex = 0;
        for (int i = activeGoalIndex + 1; i < goals.length; i++) {
            ProximaGoal goal = goals[i];

            if (!goal.shouldStart()) {
                break;
            }

            target = goal;
            targetIndex = i;
        }

        if (target != null) {
            activeGoal.end();

            activeGoal = target;
            activeGoalIndex = targetIndex;

            target.start();
            return;
        }

        if (activeGoal.shouldEnd()) {
            for (int i = activeGoalIndex - 1; i >= 0 && i < goals.length; i--) {
                ProximaGoal goal = goals[i];

                if (goal.shouldStart()) {
                    target = goal;
                    targetIndex = i;
                    break;
                }
            }

            activeGoal.end();

            activeGoal = target;
            activeGoalIndex = targetIndex;
            return;
        }

        activeGoal.tick(time);
    }

    @Override
    public @Nullable ProximaGoal currentGoal() {
        return activeGoal;
    }
}
