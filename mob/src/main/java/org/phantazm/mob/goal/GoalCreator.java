package org.phantazm.mob.goal;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.proxima.bindings.minestom.goal.ProximaGoal;

public interface GoalCreator {
    @NotNull ProximaGoal create(@NotNull PhantazmMob mob);
}
