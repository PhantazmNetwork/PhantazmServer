package org.phantazm.zombies.event.entity;

import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.event.trait.MobTargetEvent;

import java.util.Objects;

public class MobDeathEvent implements MobTargetEvent, EntityInstanceEvent {
    private final Mob mob;

    public MobDeathEvent(@NotNull Mob mob) {
        this.mob = Objects.requireNonNull(mob);
    }

    @Override
    public @NotNull Mob target() {
        return mob;
    }
}
