package com.github.phantazmnetwork.zombies.game.player.state;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record PlayerStateKey<TContext>(@NotNull Key key) implements Keyed {

    public PlayerStateKey {
        Objects.requireNonNull(key, "key");
    }

}
