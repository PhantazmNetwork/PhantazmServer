package org.phantazm.core.game.scene;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface SceneRouter<TScene, TRequest extends SceneJoinRequest> extends Scene<TRequest> {

    @NotNull Collection<TScene> getScenes();

    @NotNull Optional<TScene> getScene(@NotNull UUID playerUUID);

}
