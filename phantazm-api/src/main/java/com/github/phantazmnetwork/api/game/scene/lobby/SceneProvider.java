package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.game.scene.Scene;
import com.github.phantazmnetwork.api.util.Tickable;
import org.jetbrains.annotations.NotNull;

public interface SceneProvider<T extends Scene<?>> extends Tickable {

    @NotNull T provideScene();

    void forceShutdown();

}
