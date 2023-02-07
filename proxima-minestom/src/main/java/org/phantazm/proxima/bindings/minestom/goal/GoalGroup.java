package org.phantazm.proxima.bindings.minestom.goal;

import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.Tickable;

public interface GoalGroup extends Tickable {
    @Nullable ProximaGoal currentGoal();
}
