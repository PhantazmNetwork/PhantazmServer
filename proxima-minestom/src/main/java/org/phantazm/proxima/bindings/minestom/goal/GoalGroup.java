package org.phantazm.proxima.bindings.minestom.goal;

import net.minestom.server.Tickable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

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
     * The goals managed by this goal group, as an immutable list.
     *
     * @return the goals managed by this goal group; cannot be modified
     */
    @NotNull
    @Unmodifiable
    List<ProximaGoal> goals();
}