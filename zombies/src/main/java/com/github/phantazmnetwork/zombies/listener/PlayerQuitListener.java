package com.github.phantazmnetwork.zombies.listener;

import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.player.state.ZombiesPlayerStateKeys;
import com.github.phantazmnetwork.zombies.player.state.context.NoContext;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class PlayerQuitListener extends ZombiesPlayerEventListener<PlayerDisconnectEvent> {
    public PlayerQuitListener(@NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers) {
        super(instance, zombiesPlayers);
    }

    @Override
    protected void accept(@NotNull ZombiesPlayer zombiesPlayer, @NotNull PlayerDisconnectEvent event) {
        zombiesPlayer.setState(ZombiesPlayerStateKeys.QUIT, NoContext.INSTANCE);
    }
}
