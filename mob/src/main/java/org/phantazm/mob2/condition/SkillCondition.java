package org.phantazm.mob2.condition;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;

public interface SkillCondition {
    boolean test(@NotNull Mob mob);
}
