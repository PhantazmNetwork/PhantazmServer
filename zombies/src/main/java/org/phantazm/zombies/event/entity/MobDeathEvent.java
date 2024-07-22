package org.phantazm.zombies.event.entity;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.event.trait.MobInstanceEvent;

import java.util.Objects;

public class MobDeathEvent implements MobInstanceEvent {
    private final Mob mob;

    public MobDeathEvent(@NotNull Mob mob) {
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
