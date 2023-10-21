package org.phantazm.core.tick;

import net.minestom.server.Tickable;

public interface Activable extends Tickable {

    default void start() {

    }

    default void tick(long time) {

    }

    default void end() {

    }

}
