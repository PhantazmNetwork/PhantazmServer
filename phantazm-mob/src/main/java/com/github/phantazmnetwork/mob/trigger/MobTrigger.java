package com.github.phantazmnetwork.mob.trigger;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a trigger that a mob can use based on an {@link Event}.
 * @param key The unique {@link Key} of the trigger
 * @param eventClass The {@link Class} of the {@link Event} associated with the trigger
 * @param entityGetter A {@link Function} that returns the {@link Entity} associated with the {@link Event}
 * @param <TEvent> The event type associated with the trigger
 */
public record MobTrigger<TEvent extends Event>(@NotNull Key key,
                                               @NotNull Class<TEvent> eventClass,
                                               @NotNull Function<TEvent, Entity> entityGetter) {

    /**
     * Creates a {@link MobTrigger} instance.
     * @param key The unique {@link Key} of the trigger
     * @param eventClass The {@link Class} of the {@link Event} associated with the trigger
     * @param entityGetter A {@link Function} that returns the {@link Entity} associated with the {@link Event}
     */
    public MobTrigger {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(eventClass, "eventClass");
        Objects.requireNonNull(entityGetter, "entityGetter");
    }

}
