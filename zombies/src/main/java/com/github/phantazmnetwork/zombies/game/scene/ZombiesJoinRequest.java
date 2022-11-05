package com.github.phantazmnetwork.zombies.game.scene;

import com.github.phantazmnetwork.core.game.scene.SceneJoinRequest;
import com.github.phantazmnetwork.core.player.PlayerView;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface ZombiesJoinRequest extends SceneJoinRequest {

    @NotNull Collection<PlayerView> getPlayers();

    @Override
    default int getRequestWeight() {
        return getPlayers().size();
    }
}
