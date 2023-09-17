package org.phantazm.core.scene2;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;

import java.util.Set;

public interface SceneCreator<T extends Scene> {
    @NotNull T createScene();

    int sceneCap();

    int playerCap();

    boolean hasPermission(@NotNull Set<? extends @NotNull PlayerView> players);
}
