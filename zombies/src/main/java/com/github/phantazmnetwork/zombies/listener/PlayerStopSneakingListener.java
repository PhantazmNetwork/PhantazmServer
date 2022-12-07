package com.github.phantazmnetwork.zombies.listener;

import com.github.phantazmnetwork.zombies.map.handler.WindowHandler;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import net.minestom.server.event.player.PlayerStartSneakingEvent;
import net.minestom.server.event.player.PlayerStopSneakingEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerStopSneakingListener extends ZombiesPlayerEventListener<PlayerStopSneakingEvent> {
    private final WindowHandler windowHandler;

    public PlayerStopSneakingListener(@NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers, @NotNull WindowHandler windowHandler) {
        super(instance, zombiesPlayers);
        this.windowHandler = Objects.requireNonNull(windowHandler, "windowHandler");
    }

    @Override
    protected void accept(@NotNull ZombiesPlayer zombiesPlayer, @NotNull PlayerStopSneakingEvent event) {
        windowHandler.handleCrouchStateChange(zombiesPlayer, false);
    }
}
