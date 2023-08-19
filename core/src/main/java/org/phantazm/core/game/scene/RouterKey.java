package org.phantazm.core.game.scene;

import org.jetbrains.annotations.NotNull;

public record RouterKey<TScene extends Scene<?>, TRequest extends SceneJoinRequest, TRouter extends SceneRouter<TScene, TRequest>>(
    @NotNull Class<TRouter> router) {
}
