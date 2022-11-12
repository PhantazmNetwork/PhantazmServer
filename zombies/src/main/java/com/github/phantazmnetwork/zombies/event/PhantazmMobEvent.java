package com.github.phantazmnetwork.zombies.event;

import com.github.phantazmnetwork.mob.PhantazmMob;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record PhantazmMobEvent<TEvent extends Event>(@NotNull PhantazmMob mob, @NotNull TEvent event) {

    public PhantazmMobEvent {
        Objects.requireNonNull(mob, "mob");
        Objects.requireNonNull(event, "event");
    }

}
