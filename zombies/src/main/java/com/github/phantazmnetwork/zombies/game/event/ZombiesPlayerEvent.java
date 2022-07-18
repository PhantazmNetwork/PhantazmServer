package com.github.phantazmnetwork.zombies.game.event;

import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record ZombiesPlayerEvent<TEvent extends Event>(@NotNull ZombiesPlayer zombiesPlayer, @NotNull TEvent event) {

    public ZombiesPlayerEvent {
        Objects.requireNonNull(zombiesPlayer, "zombiesPlayer");
        Objects.requireNonNull(event, "event");
    }

}
