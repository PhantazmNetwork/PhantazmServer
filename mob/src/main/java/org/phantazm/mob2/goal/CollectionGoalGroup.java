package org.phantazm.mob2.goal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.proxima.bindings.minestom.goal.GoalGroup;
import org.phantazm.proxima.bindings.minestom.goal.ProximaGoal;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class CollectionGoalGroup implements GoalGroup {
    private final List<ProximaGoal> goals;
    private volatile ProximaGoal active;

    public CollectionGoalGroup(@NotNull Collection<ProximaGoal> goals) {
        this.goals = new CopyOnWriteArrayList<>(goals);
    }

    @Override
    public void tick(long time) {
        ProximaGoal active = this.active;

        // current active goal requested termination
        if (active != null && active.shouldEnd()) {
            active.end();
            this.active = active = null;
        }

        boolean foundActive = false;
        for (ProximaGoal goal : goals) {
            if (goal == active) {
                foundActive = true;
                break;
            }

            if (!goal.shouldStart()) continue;

            if (active != null) active.end();

            goal.start();
            goal.tick(time);
            this.active = goal;
            return;
        }

        if (active != null) {
            if (!foundActive) {
                // end any active goal that's not in the goals list anymore
                // this happens if the active goal is removed using removeGoal
                active.end();
                this.active = null;
            } else active.tick(time);
        }
    }

    @Override
    public @NotNull Optional<ProximaGoal> currentGoal() {
        return Optional.ofNullable(this.active);
    }

    @Override
    public @NotNull @UnmodifiableView List<ProximaGoal> goals() {
        return Collections.unmodifiableList(this.goals);
    }

    @Override
    public void addGoal(int index, @NotNull ProximaGoal goal) {
        this.goals.add(index, goal);
    }

    @Override
    public void addGoal(@NotNull ProximaGoal goal) {
        this.goals.add(goal);
    }

    @Override
    public @NotNull ProximaGoal removeGoal(int index) {
        return this.goals.remove(index);
    }

    @Override
    public boolean removeGoal(@NotNull ProximaGoal goal) {
        return this.goals.remove(goal);
    }
}