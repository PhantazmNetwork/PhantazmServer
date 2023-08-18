package org.phantazm.mob2.trigger;

import org.jetbrains.annotations.NotNull;

public enum Trigger {
    DEATH("death"),
    SPAWN("spawn"),
    ATTACK("attack"),
    INTERACT("interact"),
    DAMAGED("damaged");

    private final String name;

    Trigger(String name) {
        this.name = name;
    }

    public @NotNull String id() {
        return name;
    }
}
