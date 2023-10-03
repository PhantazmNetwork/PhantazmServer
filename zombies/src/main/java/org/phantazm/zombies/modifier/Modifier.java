package org.phantazm.zombies.modifier;

import net.minestom.server.Tickable;

public interface Modifier extends Tickable {
    void apply();

    default void tick(long time) {
    }
}
