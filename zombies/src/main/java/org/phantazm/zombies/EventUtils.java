package org.phantazm.zombies;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.event.player.ZombiesPlayerDamageEvent;
import org.phantazm.zombies.event.trait.EntityTargetEvent;

/**
 * Common utilities relating to events.
 */
public final class EventUtils {
    private EventUtils() {
    }

    /**
     * Extracts an entity from an {@link EntityEvent}, encapsulating common logic around getting the "target" of an
     * event (see {@link EntityTargetEvent}), and any necessary special-casing for certain kinds of events.
     *
     * @param event     the event
     * @param getTarget whether to extract the "target" of the event
     * @return the entity
     */
    public static @NotNull Entity extractEntity(@NotNull EntityEvent event, boolean getTarget) {
        // simplest case: we want the target, and have an EntityTargetEvent
        if (getTarget && event instanceof EntityTargetEvent entityTargetEvent)
            return entityTargetEvent.target();

        // more complex case: we want the source, and have a ZombiesPlayerDamageEvent
        if (!getTarget && event instanceof ZombiesPlayerDamageEvent zombiesPlayerDamageEvent) {
            // prefer the attacker...
            Entity attacker = zombiesPlayerDamageEvent.damage().getAttacker();

            // ...but use the source as a fallback
            if (attacker == null) attacker = zombiesPlayerDamageEvent.damage().getSource();

            // if we can't get the source, use the player as a final failsafe
            return attacker != null ? attacker : zombiesPlayerDamageEvent.getPlayer();
        }

        return event.getEntity();
    }
}
