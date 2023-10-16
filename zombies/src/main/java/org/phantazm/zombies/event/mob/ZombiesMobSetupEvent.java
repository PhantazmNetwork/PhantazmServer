package org.phantazm.zombies.event.mob;

import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Objects;

/**
 * Event broadcast by a {@link ZombiesScene} when a mob is spawned, but before it is added to an instance. Since the mob
 * is <i>not</i> being ticked yet, and this event is broadcast synchronously, it is not necessary to acquire the mob in
 * order to call methods on it.
 */
public class ZombiesMobSetupEvent implements EntityInstanceEvent {
    private final Mob mob;

    public ZombiesMobSetupEvent(@NotNull Mob mob) {
        this.mob = Objects.requireNonNull(mob);
    }

    @Override
    public @NotNull Mob getEntity() {
        return mob;
    }
}
