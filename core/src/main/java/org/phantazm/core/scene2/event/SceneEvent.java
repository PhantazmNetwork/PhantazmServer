package org.phantazm.core.scene2.event;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.scene2.Scene;

public interface SceneEvent extends Event {
    @NotNull Scene scene();
}
