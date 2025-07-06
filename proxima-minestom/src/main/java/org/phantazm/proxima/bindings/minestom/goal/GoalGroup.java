package org.phantazm.proxima.bindings.minestom.goal;

import net.minestom.server.Tickable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;
import java.util.Optional;

/**
 * A class responsible for managing an entity's AI goal(s). Must be ticked in order to progress.
 */
public interface GoalGroup extends Tickable {
    /**
     * The currently active goal, if any.
     *
     * @return the currently active goal, or empty if none
     */
    @NotNull Optional<ProximaGoal> currentGoal();

    /**
     * The goals managed by this goal group.
     *
     * @return the goals managed by this goal group; cannot be modified, but may change as a result of calls to other
     * methods
     */
    @NotNull
    @UnmodifiableView
    List<ProximaGoal> goals();

    void addGoal(int index, @NotNull ProximaGoal goal);

    void addGoal(@NotNull ProximaGoal goal);

    @NotNull ProximaGoal removeGoal(int index);

    boolean removeGoal(@NotNull ProximaGoal goal);
}