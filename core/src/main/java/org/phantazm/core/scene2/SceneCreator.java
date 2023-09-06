package org.phantazm.core.scene2;

import org.jetbrains.annotations.NotNull;

public interface SceneCreator<T extends Scene> {
    @NotNull T createScene();

    int sceneCap();

    int playerCap();
}
