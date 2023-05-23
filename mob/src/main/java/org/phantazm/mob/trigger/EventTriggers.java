package org.phantazm.mob.trigger;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.entity.EntitySpawnEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.event.trait.EntityEvent;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Namespaces;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * {@link EventTrigger}s that should automatically be recognized by Phantazm. All triggers of this type are initialized
 * at arena startup.
 */
public class EventTriggers {

    /**
     * A {@link EventTrigger} for when an {@link Entity} is damaged.
     */
    public static final EventTrigger<EntityDamageEvent> DAMAGE_TRIGGER =
            regularTrigger("damage", EntityDamageEvent.class);
    /**
     * A {@link EventTrigger} for when an {@link Entity} is spawned.
     */
    public static final EventTrigger<EntitySpawnEvent> SPAWN_TRIGGER = regularTrigger("spawn", EntitySpawnEvent.class);
    /**
     * A {@link EventTrigger} for when an {@link Entity} is killed.
     */
    public static final EventTrigger<EntityDeathEvent> DEATH_TRIGGER = regularTrigger("death", EntityDeathEvent.class);
    /**
     * A {@link EventTrigger} for when a {@link Player} interacts with an {@link Entity}.
     */
    public static final EventTrigger<PlayerEntityInteractEvent> INTERACT_TRIGGER =
            trigger("interact", PlayerEntityInteractEvent.class, PlayerEntityInteractEvent::getTarget);
    /**
     * A {@link Collection} of default {@link EventTrigger}s.
     */
    public static final Collection<EventTrigger<?>> TRIGGERS =
            List.of(DAMAGE_TRIGGER, SPAWN_TRIGGER, DEATH_TRIGGER, INTERACT_TRIGGER);

    private EventTriggers() {
        throw new UnsupportedOperationException();
    }

    private static <TEvent extends Event> @NotNull EventTrigger<TEvent> trigger(@Subst("interact") @NotNull String name,
            @NotNull Class<TEvent> eventClass, @NotNull Function<TEvent, Entity> entityGetter) {
        return new EventTrigger<>(Key.key(Namespaces.PHANTAZM, name), eventClass, entityGetter);
    }

    private static <TEvent extends EntityEvent> @NotNull EventTrigger<TEvent> regularTrigger(
            @Subst("damage") @NotNull String name, @NotNull Class<TEvent> eventClass) {
        return trigger(name, eventClass, EntityEvent::getEntity);
    }

}
