package org.phantazm.core.scene2.event;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.scene2.Scene;

import java.util.Objects;

public record SceneCreationEvent(Scene scene) implements SceneEvent {
    public SceneCreationEvent(@NotNull Scene scene) {
        this.scene = Objects.requireNonNull(scene);
    }


    @Override
    public @NotNull Scene scene() {
        return scene;
    }
}
