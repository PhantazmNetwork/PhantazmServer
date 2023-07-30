package org.phantazm.zombies.scene;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.SceneJoinRequest;
import org.phantazm.core.player.PlayerView;

import java.util.Collection;
import java.util.UUID;

public interface ZombiesJoinRequest extends SceneJoinRequest {

    @NotNull Collection<PlayerView> getPlayers();

    @NotNull UUID getUUID();

    boolean isRestricted();

    @Override
    default int getRequestWeight() {
        return getPlayers().size();
    }
}
