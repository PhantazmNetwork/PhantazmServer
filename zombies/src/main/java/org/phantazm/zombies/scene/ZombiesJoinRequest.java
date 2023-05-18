package org.phantazm.zombies.scene;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.SceneJoinRequest;
import org.phantazm.core.player.PlayerView;

import java.util.Collection;

public interface ZombiesJoinRequest extends SceneJoinRequest {

    @NotNull Collection<PlayerView> getPlayers();

    @Override
    default int getRequestWeight() {
        return getPlayers().size();
    }
}
