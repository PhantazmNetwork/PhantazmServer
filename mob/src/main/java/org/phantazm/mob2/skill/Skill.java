package org.phantazm.mob2.skill;

public interface Skill {
    default void init() {
    }

    void use();

    default void tick() {
    }

    default boolean needsTicking() {
        return false;
    }

    default void end() {
    }
}
