package org.phantazm.core.event;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record PlayerJoinLobbyEvent(Player player) implements PlayerEvent {
    public PlayerJoinLobbyEvent(@NotNull Player player) {
        this.player = Objects.requireNonNull(player, "player");
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
