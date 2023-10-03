package org.phantazm.zombies.modifier;

import net.kyori.adventure.key.Keyed;
import net.minestom.server.Tickable;

public interface Modifier extends Tickable, Keyed {
    void apply();

    default void tick(long time) {
    }
}
