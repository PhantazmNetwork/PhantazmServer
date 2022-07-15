package com.github.phantazmnetwork.zombies.game;

import com.github.phantazmnetwork.api.game.scene.SceneJoinRequest;
import com.github.phantazmnetwork.api.player.PlayerView;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface ZombiesJoinRequest extends SceneJoinRequest {

    @NotNull Collection<PlayerView> getPlayers();

}
