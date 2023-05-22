package org.phantazm.mob.goal;

import org.jetbrains.annotations.NotNull;
import org.phantazm.proxima.bindings.minestom.goal.GoalGroup;
import org.phantazm.proxima.bindings.minestom.goal.ProximaGoal;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class CollectionGoalGroup implements GoalGroup {
    private final ProximaGoal[] goals;

    private ProximaGoal activeGoal;

    public CollectionGoalGroup(@NotNull Collection<ProximaGoal> goals) {
        this.goals = goals.toArray(ProximaGoal[]::new);
        for (ProximaGoal goal : this.goals) {
            Objects.requireNonNull(goal, "entity AI goal");
        }
    }

    @Override
    public void tick(long time) {
        ProximaGoal activeGoal = this.activeGoal;

        if (activeGoal != null && activeGoal.shouldEnd()) {
            activeGoal.end();
            this.activeGoal = activeGoal = null;
        }

        for (ProximaGoal goal : goals) {
            if (goal == activeGoal) {
                break;
            }

            if (goal.shouldStart()) {
                if (activeGoal != null) {
                    activeGoal.end();
                }

                this.activeGoal = activeGoal = goal;
                activeGoal.start();
                break;
            }
        }

        if (activeGoal != null) {
            activeGoal.tick(time);
        }
    }

    @Override
    public @NotNull Optional<ProximaGoal> currentGoal() {
        return Optional.ofNullable(activeGoal);
    }
}
