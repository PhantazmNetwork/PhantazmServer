package org.phantazm.mob2.goal;

import com.github.steanky.toolkit.collection.Containers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.proxima.bindings.minestom.goal.GoalGroup;
import org.phantazm.proxima.bindings.minestom.goal.ProximaGoal;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CollectionGoalGroup implements GoalGroup {
    private final ProximaGoal[] goals;

    private ProximaGoal activeGoal;

    public CollectionGoalGroup(@NotNull Collection<ProximaGoal> goals) {
        this.goals = goals.toArray(ProximaGoal[]::new);

        for (ProximaGoal goal : this.goals) Objects.requireNonNull(goal);
    }

    @Override
    public void tick(long time) {
        ProximaGoal activeGoal = this.activeGoal;

        if (activeGoal != null && activeGoal.shouldEnd()) {
            activeGoal.end();
            this.activeGoal = activeGoal = null;
        }

        // Search all previous goals up until the currently enabled one. If any want to be activated, end the current
        // one, and start the new one that requested activation. This has the effect that goals earlier in the list have
        // priority over later ones.
        //
        // activeGoal will be null here when it wants to end (shouldEnd returns true). If this is the case, we simply
        // activate the first goal in the list that wants to start.
        for (ProximaGoal goal : goals) {
            if (goal == activeGoal) break;
            if (!goal.shouldStart()) continue;

            if (activeGoal != null) activeGoal.end();

            this.activeGoal = activeGoal = goal;
            activeGoal.start();
            break;
        }

        if (activeGoal != null) activeGoal.tick(time);
    }

    @Override
    public @NotNull Optional<ProximaGoal> currentGoal() {
        return Optional.ofNullable(activeGoal);
    }

    @Override
    public @NotNull @Unmodifiable List<ProximaGoal> goals() {
        return Containers.arrayView(goals);
    }
}