package org.phantazm.zombies.event.trait;

import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

public interface DamageEvent extends Event {
    @NotNull Damage damage();
}
