package org.phantazm.mob2.skill;

import org.jetbrains.annotations.Nullable;
import org.phantazm.mob2.Trigger;

public interface Skill {
    @Nullable Trigger trigger();

    default void init() {
    }

    default void use() {
    }

    default void tick() {
    }

    default boolean needsTicking() {
        return false;
    }

    default void end() {
    }
}
