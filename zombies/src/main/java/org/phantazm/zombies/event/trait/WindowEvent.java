package org.phantazm.zombies.event.trait;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Window;

public interface WindowEvent extends Event {
    @NotNull Window window();
}
