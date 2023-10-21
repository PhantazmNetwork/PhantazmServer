package org.phantazm.proxima.bindings.minestom.goal;

import net.minestom.server.Tickable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;

public interface GoalGroup extends Tickable {
    @NotNull Optional<ProximaGoal> currentGoal();

    @NotNull @Unmodifiable List<ProximaGoal> goals();
}
