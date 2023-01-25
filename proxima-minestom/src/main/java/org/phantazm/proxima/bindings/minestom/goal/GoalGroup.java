package org.phantazm.proxima.bindings.minestom.goal;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Tickable;

import java.util.Collection;
import java.util.Objects;

/**
 * Represents a group of {@link ProximaGoal}s.
 * Only one {@link ProximaGoal} can be active at a time in a group.
 */
@Model("proxima.goal.group")
@Cache(false)
public class GoalGroup implements Tickable {
    private final ProximaGoal[] goals;

    private ProximaGoal activeGroup;
    private int activeGroupIndex;

    /**
     * Creates a {@link GoalGroup}.
     *
     * @param goals The {@link ProximaGoal}s in the group
     */
    @FactoryMethod
    public GoalGroup(@NotNull @Child("goals") Collection<ProximaGoal> goals) {
        this.goals = goals.toArray(ProximaGoal[]::new);
        this.activeGroupIndex = 0;
    }

    @Override
    public void tick(long time) {
        if (activeGroup == null) {
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
                activeGroup = target;
                activeGroupIndex = targetIndex;

                target.start();
            }

            return;
        }

        ProximaGoal target = null;
        int targetIndex = 0;
        for (int i = activeGroupIndex + 1; i < goals.length; i++) {
            ProximaGoal goal = goals[i];

            if (!goal.shouldStart()) {
                break;
            }

            target = goal;
            targetIndex = i;
        }

        if (target != null) {
            activeGroup.end();

            activeGroup = target;
            activeGroupIndex = targetIndex;

            target.start();
            return;
        }

        if (activeGroup.shouldEnd()) {
            for (int i = activeGroupIndex - 1; i >= 0 && i < goals.length; i--) {
                ProximaGoal goal = goals[i];

                if (goal.shouldStart()) {
                    target = goal;
                    targetIndex = i;
                    break;
                }
            }

            activeGroup.end();

            activeGroup = target;
            activeGroupIndex = targetIndex;
            return;
        }

        activeGroup.tick(time);
    }

    @DataObject
    public record Data(@NotNull @ChildPath("goals") Collection<String> goalPaths) {

        public Data {
            Objects.requireNonNull(goalPaths, "goalPaths");
        }

    }

}
