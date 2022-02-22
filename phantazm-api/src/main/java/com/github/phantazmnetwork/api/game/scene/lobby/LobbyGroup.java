package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.util.Tickable;
import org.jetbrains.annotations.NotNull;

public interface LobbyGroup extends Tickable {

    @NotNull Lobby getLobby();

}
