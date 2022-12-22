package org.phantazm.zombies.event;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;

import java.util.Objects;

public record PhantazmMobEvent<TEvent extends Event>(@NotNull PhantazmMob mob, @NotNull TEvent event) {

    public PhantazmMobEvent {
        Objects.requireNonNull(mob, "mob");
        Objects.requireNonNull(event, "event");
    }

}
