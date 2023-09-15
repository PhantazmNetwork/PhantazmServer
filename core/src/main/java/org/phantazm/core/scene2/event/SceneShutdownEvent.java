package org.phantazm.core.scene2.event;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.scene2.Scene;

import java.util.Objects;

public class SceneShutdownEvent implements SceneEvent {
    private final Scene scene;

    public SceneShutdownEvent(@NotNull Scene scene) {
        this.scene = Objects.requireNonNull(scene);
    }

    @Override
    public @NotNull Scene getScene() {
        return scene;
    }
}
