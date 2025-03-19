package org.phantazm.zombies.event;

import net.minestom.server.event.Event;

public class RoundStartEvent implements Event {
    private final int round;

    public RoundStartEvent(int round) {
        this.round = round;
    }

    public int round() {
        return round;
    }
}
