package org.phantazm.mob2.goal;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;

public interface GoalApplier {
    void apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore);
}
