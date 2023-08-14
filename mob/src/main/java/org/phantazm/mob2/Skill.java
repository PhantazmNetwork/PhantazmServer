package org.phantazm.mob2;

public interface Skill {
    void init();

    void tick(long time);

    default boolean needsTicking() {
        return false;
    }

    void end();
}
