package org.phantazm.mob2.skill;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Trigger;

public interface Skill {
    default @Nullable Trigger trigger() {
        return null;
    }

    default void init(@NotNull Mob mob) {
    }

    default void use(@NotNull Mob mob) {
    }

    default void tick(@NotNull Mob mob) {
    }

    default boolean needsTicking() {
        return false;
    }

    default void end(@NotNull Mob mob) {
    }
}
