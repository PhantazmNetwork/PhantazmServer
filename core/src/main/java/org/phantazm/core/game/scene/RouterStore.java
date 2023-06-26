package org.phantazm.core.game.scene;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface RouterStore {
    @NotNull <TScene extends Scene<?>, TRequest extends SceneJoinRequest, TRouter extends SceneRouter<TScene, TRequest>> TRouter getRouter(
            @NotNull RouterKey<TScene, TRequest, TRouter> routerKey);

    <TScene extends Scene<?>, TRequest extends SceneJoinRequest, TRouter extends SceneRouter<TScene, TRequest>> void putRouter(
            @NotNull RouterKey<TScene, TRequest, TRouter> key, @NotNull SceneRouter<TScene, TRequest> router);

    @NotNull @Unmodifiable Collection<SceneRouter<?, ?>> getRouters();

    @NotNull Optional<? extends Scene<?>> getCurrentScene(@NotNull UUID uuid);
}
