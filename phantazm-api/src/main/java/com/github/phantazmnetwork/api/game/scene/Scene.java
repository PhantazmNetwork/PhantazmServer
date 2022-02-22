package com.github.phantazmnetwork.api.game.scene;

import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.api.util.Tickable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

public interface Scene<T> extends Tickable {

    @NotNull JoinResult join(@NotNull T joinRequest);

    @UnmodifiableView @NotNull Iterable<PlayerView> getPlayers();

    int getOnlinePlayerCount();

    boolean isShutdown();

    void forceShutdown();

}
