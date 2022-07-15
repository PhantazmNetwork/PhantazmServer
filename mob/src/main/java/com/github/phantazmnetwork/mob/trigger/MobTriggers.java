package com.github.phantazmnetwork.mob.trigger;

import com.github.phantazmnetwork.commons.Namespaces;
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

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * {@link MobTrigger}s that should automatically be recognized by Phantazm.
 */
public class MobTriggers {

    /**
     * A {@link MobTrigger} for when an {@link Entity} is damaged.
     */
    public static final MobTrigger<EntityDamageEvent> DAMAGE_TRIGGER =
            regularTrigger("damage", EntityDamageEvent.class);
    /**
     * A {@link MobTrigger} for when an {@link Entity} is spawned.
     */
    public static final MobTrigger<EntitySpawnEvent> SPAWN_TRIGGER = regularTrigger("spawn", EntitySpawnEvent.class);
    /**
     * A {@link MobTrigger} for when an {@link Entity} is killed.
     */
    public static final MobTrigger<EntityDeathEvent> DEATH_TRIGGER = regularTrigger("death", EntityDeathEvent.class);
    /**
     * A {@link MobTrigger} for when a {@link Player} interacts with an {@link Entity}.
     */
    public static final MobTrigger<PlayerEntityInteractEvent> INTERACT_TRIGGER =
            trigger("interact", PlayerEntityInteractEvent.class, PlayerEntityInteractEvent::getTarget);
    /**
     * A {@link Collection} of default {@link MobTrigger}s.
     */
    public static final Collection<MobTrigger<?>> TRIGGERS =
            List.of(DAMAGE_TRIGGER, SPAWN_TRIGGER, DEATH_TRIGGER, INTERACT_TRIGGER);

    private MobTriggers() {
        throw new UnsupportedOperationException();
    }

    private static <TEvent extends Event> @NotNull MobTrigger<TEvent> trigger(@Subst("interact") @NotNull String name,
                                                                              @NotNull Class<TEvent> eventClass,
                                                                              @NotNull Function<TEvent, Entity> entityGetter) {
        return new MobTrigger<>(Key.key(Namespaces.PHANTAZM, name), eventClass, entityGetter);
    }

    private static <TEvent extends EntityEvent> @NotNull MobTrigger<TEvent> regularTrigger(
            @Subst("damage") @NotNull String name, @NotNull Class<TEvent> eventClass) {
        return trigger(name, eventClass, EntityEvent::getEntity);
    }

}
