package org.phantazm.zombies.event.trait;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Door;

public interface DoorEvent extends Event {
    @NotNull Door door();
}
