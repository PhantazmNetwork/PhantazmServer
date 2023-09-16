package org.phantazm.zombies.listener;

import net.minestom.server.event.player.PlayerStartSneakingEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.map.handler.WindowHandler;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerStartSneakingListener extends ZombiesPlayerEventListener<PlayerStartSneakingEvent> {
    private final WindowHandler windowHandler;

    public PlayerStartSneakingListener(@NotNull Instance instance,
        @NotNull Map<PlayerView, ZombiesPlayer> zombiesPlayers, @NotNull WindowHandler windowHandler) {
        super(instance, zombiesPlayers);
        this.windowHandler = Objects.requireNonNull(windowHandler);
    }

    @Override
    protected void accept(@NotNull ZombiesPlayer zombiesPlayer, @NotNull PlayerStartSneakingEvent event) {
        windowHandler.handleCrouchStateChange(zombiesPlayer, true);
    }
}
