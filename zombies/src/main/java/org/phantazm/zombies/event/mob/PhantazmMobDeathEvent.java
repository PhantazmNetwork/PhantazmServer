package org.phantazm.zombies.event.mob;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;

import java.util.Objects;

public class PhantazmMobDeathEvent implements ZombiesMobDeathEvent {
    private final Mob mob;

    public PhantazmMobDeathEvent(@NotNull Mob mob) {
        this.mob = Objects.requireNonNull(mob);
    }

    @Override
    public @NotNull Entity getEntity() {
        return mob;
    }

    @Override
    public @NotNull Mob mob() {
        return mob;
    }
}
