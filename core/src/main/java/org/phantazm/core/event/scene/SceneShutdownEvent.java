package org.phantazm.core.event.scene;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.scene2.Scene;

import java.util.Objects;

public record SceneShutdownEvent(Scene scene) implements SceneEvent {
    public SceneShutdownEvent(@NotNull Scene scene) {
        this.scene = Objects.requireNonNull(scene);
    }

    @Override
    public @NotNull Scene scene() {
        return scene;
    }
}
