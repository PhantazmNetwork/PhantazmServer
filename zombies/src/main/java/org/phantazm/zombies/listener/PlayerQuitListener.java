package org.phantazm.zombies.listener;

import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;
import org.phantazm.zombies.player.state.context.NoContext;

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
