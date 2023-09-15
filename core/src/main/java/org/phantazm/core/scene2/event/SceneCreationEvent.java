package org.phantazm.core.scene2.event;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.scene2.Scene;

public class SceneCreationEvent implements SceneEvent {
    private final Scene scene;

    public SceneCreationEvent(@NotNull Scene scene) {
        this.scene = scene;
    }


    @Override
    public @NotNull Scene getScene() {
        return scene;
    }
}
