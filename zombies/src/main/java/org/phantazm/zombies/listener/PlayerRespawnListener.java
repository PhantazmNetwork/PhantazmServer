package org.phantazm.zombies.listener;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerRespawnEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class PlayerRespawnListener extends ZombiesPlayerEventListener<PlayerRespawnEvent> {
    private final Pos spawnPoint;

    public PlayerRespawnListener(@NotNull Instance instance, @NotNull Map<PlayerView, ZombiesPlayer> zombiesPlayers,
        @NotNull Pos spawnPoint, @NotNull Supplier<ZombiesScene> scene) {
        super(instance, zombiesPlayers, scene);
        this.spawnPoint = Objects.requireNonNull(spawnPoint);
    }

    @Override
    protected void accept(@NotNull ZombiesScene scene, @NotNull ZombiesPlayer zombiesPlayer, @NotNull PlayerRespawnEvent event) {
        event.setRespawnPosition(spawnPoint);
    }
}
