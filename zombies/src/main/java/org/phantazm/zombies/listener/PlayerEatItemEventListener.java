package org.phantazm.zombies.listener;

import net.minestom.server.event.player.PlayerPreEatEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Map;
import java.util.UUID;

public class PlayerEatItemEventListener extends ZombiesPlayerEventListener<PlayerPreEatEvent> {
    public PlayerEatItemEventListener(@NotNull Instance instance,
        @NotNull Map<PlayerView, ZombiesPlayer> zombiesPlayers) {
        super(instance, zombiesPlayers);
    }

    @Override
    protected void accept(@NotNull ZombiesPlayer zombiesPlayer, @NotNull PlayerPreEatEvent event) {
        event.setCancelled(true);
    }
}
