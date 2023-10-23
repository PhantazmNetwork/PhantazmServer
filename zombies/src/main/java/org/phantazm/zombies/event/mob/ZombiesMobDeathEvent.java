package org.phantazm.zombies.event.mob;

import net.minestom.server.event.trait.EntityEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;

public interface ZombiesMobDeathEvent extends EntityEvent {
    @NotNull
    Mob mob();
}
