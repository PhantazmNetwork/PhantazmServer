package org.phantazm.core.game.scene.event;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.Scene;

public interface SceneEvent extends Event {
    @NotNull Scene<?> getScene();
}
