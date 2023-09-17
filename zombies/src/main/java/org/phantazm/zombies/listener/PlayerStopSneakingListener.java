package org.phantazm.zombies.listener;

import net.minestom.server.event.player.PlayerStopSneakingEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.map.handler.WindowHandler;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class PlayerStopSneakingListener extends ZombiesPlayerEventListener<PlayerStopSneakingEvent> {
    private final WindowHandler windowHandler;

    public PlayerStopSneakingListener(@NotNull Instance instance,
        @NotNull Map<PlayerView, ZombiesPlayer> zombiesPlayers, @NotNull WindowHandler windowHandler,
        @NotNull Supplier<ZombiesScene> scene) {
        super(instance, zombiesPlayers, scene);
        this.windowHandler = Objects.requireNonNull(windowHandler);
    }

    @Override
    protected void accept(@NotNull ZombiesScene scene, @NotNull ZombiesPlayer zombiesPlayer, @NotNull PlayerStopSneakingEvent event) {
        windowHandler.handleCrouchStateChange(zombiesPlayer, false);
    }
}
