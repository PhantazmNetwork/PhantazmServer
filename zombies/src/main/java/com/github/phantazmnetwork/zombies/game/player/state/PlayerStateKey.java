package com.github.phantazmnetwork.zombies.game.player.state;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("unused") // Generic parameter not technically needed, but nicer for stronger API in ZombiesPlayer
public record PlayerStateKey<TContext>(@NotNull Key key) implements Keyed {

    public PlayerStateKey {
        Objects.requireNonNull(key, "key");
    }

}
