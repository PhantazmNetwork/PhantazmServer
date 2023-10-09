package org.phantazm.core.event.scene;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.scene2.Scene;

public interface SceneEvent extends Event {
    @NotNull Scene scene();
}
