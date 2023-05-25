package org.phantazm.mob.skill;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;

public interface Skill {
    /**
     * Uses the skill.
     */
    void use(@NotNull PhantazmMob self);

    default void tick(long time, @NotNull PhantazmMob self) {
    }

    default boolean needsTicking() {
        return false;
    }
}
