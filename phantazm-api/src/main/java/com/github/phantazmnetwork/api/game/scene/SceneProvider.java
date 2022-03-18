package com.github.phantazmnetwork.api.game.scene;

import com.github.phantazmnetwork.api.util.Tickable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Optional;

public interface SceneProvider<T extends Scene<?>> extends Tickable {

    @NotNull Optional<T> provideScene();

    @UnmodifiableView @NotNull Iterable<T> listScenes();

    void forceShutdown();

}
