package org.phantazm.mob2.goal;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;

public interface GoalApplier {
    void apply(@NotNull Mob mob);
}
