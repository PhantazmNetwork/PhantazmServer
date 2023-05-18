package org.phantazm.mob.goal;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;

public interface GoalApplier {
    void apply(@NotNull PhantazmMob mob);
}
