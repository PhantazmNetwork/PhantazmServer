package org.phantazm.zombies.event;

import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;

import java.util.Objects;

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
