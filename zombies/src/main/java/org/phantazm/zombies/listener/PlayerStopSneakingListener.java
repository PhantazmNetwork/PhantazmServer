package org.phantazm.zombies.listener;

import net.minestom.server.event.player.PlayerStopSneakingEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.handler.WindowHandler;
import org.phantazm.zombies.player.ZombiesPlayer;

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
