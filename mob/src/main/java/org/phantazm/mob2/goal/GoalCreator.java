package org.phantazm.mob2.goal;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.proxima.bindings.minestom.goal.ProximaGoal;

public interface GoalCreator {
    @NotNull ProximaGoal create(@NotNull Mob mob);
}
