package org.phantazm.zombies.event;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

public record ZombiesPlayerEvent<TEvent extends Event>(@NotNull ZombiesPlayer zombiesPlayer, @NotNull TEvent event)
        implements Event {

    public ZombiesPlayerEvent {
        Objects.requireNonNull(zombiesPlayer, "zombiesPlayer");
        Objects.requireNonNull(event, "event");
    }

}
