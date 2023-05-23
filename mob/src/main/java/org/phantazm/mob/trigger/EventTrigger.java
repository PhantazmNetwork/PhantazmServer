package org.phantazm.mob.trigger;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.MobStore;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a trigger that a mob can use based on an {@link Event}.
 *
 * @param key          The unique {@link Key} of the trigger
 * @param eventClass   The {@link Class} of the {@link Event} associated with the trigger
 * @param entityGetter A {@link Function} that returns the {@link Entity} associated with the {@link Event}
 * @param <TEvent>     The event type associated with the trigger
 */
public record EventTrigger<TEvent extends Event>(@NotNull Key key,
                                                 @NotNull Class<TEvent> eventClass,
                                                 @NotNull Function<TEvent, Entity> entityGetter) implements MobTrigger {

    /**
     * Creates a {@link EventTrigger} instance.
     *
     * @param key          The unique {@link Key} of the trigger
     * @param eventClass   The {@link Class} of the {@link Event} associated with the trigger
     * @param entityGetter A {@link Function} that returns the {@link Entity} associated with the {@link Event}
     */
    public EventTrigger {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(eventClass, "eventClass");
        Objects.requireNonNull(entityGetter, "entityGetter");
    }

    @Override
    public void initialize(@NotNull EventNode<Event> node, @NotNull MobStore store) {
        node.addListener(eventClass, event -> store.useTrigger(entityGetter.apply(event), key));
    }
}
