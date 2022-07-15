package com.github.phantazmnetwork.core.player;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record PlayerInfo(@NotNull Player player, int protocol) {

    public PlayerInfo {
        Objects.requireNonNull(player, "player");
    }

}
