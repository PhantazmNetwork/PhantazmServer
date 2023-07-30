package org.phantazm.zombies.listener;

import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.TransferResult;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene.LeaveHandler;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerQuitListener extends ZombiesPlayerEventListener<PlayerDisconnectEvent> {

    private final LeaveHandler leaveHandler;

    public PlayerQuitListener(@NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers, @NotNull LeaveHandler leaveHandler) {
        super(instance, zombiesPlayers);
        this.leaveHandler = Objects.requireNonNull(leaveHandler, "leaveHandler");
    }

    @Override
    protected void accept(@NotNull ZombiesPlayer zombiesPlayer, @NotNull PlayerDisconnectEvent event) {
        try (TransferResult result = leaveHandler.leave(
                Collections.singleton(zombiesPlayer.module().getPlayerView().getUUID()))) {
            result.executor().ifPresent(Runnable::run);
        }
    }
}
