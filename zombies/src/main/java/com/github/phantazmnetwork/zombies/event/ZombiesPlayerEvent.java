package com.github.phantazmnetwork.zombies.event;

import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record ZombiesPlayerEvent<TEvent extends Event>(@NotNull ZombiesPlayer zombiesPlayer, @NotNull TEvent event)
        implements Event {

    public ZombiesPlayerEvent {
        Objects.requireNonNull(zombiesPlayer, "zombiesPlayer");
        Objects.requireNonNull(event, "event");
    }

}