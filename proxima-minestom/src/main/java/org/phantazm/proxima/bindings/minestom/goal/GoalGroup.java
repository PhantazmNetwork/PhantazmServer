package org.phantazm.proxima.bindings.minestom.goal;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Tickable;

import java.util.Optional;

public interface GoalGroup extends Tickable {
    @NotNull Optional<ProximaGoal> currentGoal();
}
