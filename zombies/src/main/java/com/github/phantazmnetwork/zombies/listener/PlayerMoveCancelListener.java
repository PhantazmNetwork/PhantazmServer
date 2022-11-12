package com.github.phantazmnetwork.zombies.listener;

import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class PlayerMoveCancelListener extends ZombiesPlayerEventListener<PlayerMoveEvent> {
    public PlayerMoveCancelListener(@NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers) {
        super(instance, zombiesPlayers);
    }

    @Override
    protected void accept(@NotNull ZombiesPlayer zombiesPlayer, @NotNull PlayerMoveEvent event) {
        if (!event.getPlayer().getPosition().asVec().equals(event.getNewPosition().asVec())) {
            event.setCancelled(true);
        }
    }
}
