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
public class GoalGroup implements Tickable {
    private final Iterable<ProximaGoal> goals;
    private ProximaGoal activeGroup;

    /**
     * Creates a {@link GoalGroup}.
     *
     * @param goals The {@link ProximaGoal}s in the group
     */
    @FactoryMethod
    public GoalGroup(@NotNull @Child("goals") Collection<ProximaGoal> goals) {
        this.goals = Objects.requireNonNull(goals, "goals");
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
        for (ProximaGoal goal : goals) {
            if (goal.shouldStart()) {
                activeGroup = goal;
                goal.start();
                break;
            }
        }
    }

    @DataObject
    public record Data(@NotNull @ChildPath("goals") Collection<String> goalPaths) {

        public Data {
            Objects.requireNonNull(goalPaths, "goalPaths");
        }

    }

}
