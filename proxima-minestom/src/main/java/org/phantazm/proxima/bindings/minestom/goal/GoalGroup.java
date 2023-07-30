package org.phantazm.proxima.bindings.minestom.goal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.Tickable;

import java.util.List;
import java.util.Optional;

public interface GoalGroup extends Tickable {
    @NotNull Optional<ProximaGoal> currentGoal();

    @NotNull @Unmodifiable List<ProximaGoal> goals();
}
