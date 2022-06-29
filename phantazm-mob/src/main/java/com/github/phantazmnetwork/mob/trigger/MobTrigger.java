package com.github.phantazmnetwork.mob.trigger;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

public record MobTrigger<TEvent extends Event>(@NotNull Key key,
                                               @NotNull Class<TEvent> eventClass,
                                               @NotNull Function<TEvent, Entity> entityGetter) {

    public MobTrigger {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(eventClass, "eventClass");
        Objects.requireNonNull(entityGetter, "entityGetter");
    }

}
