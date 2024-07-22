package org.phantazm.zombies.event.trait;

import net.minestom.server.event.trait.EntityEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;

public interface MobEvent extends EntityEvent, MobTargetEvent {
    @NotNull Mob mob();

    @Override
    default @NotNull Mob target() {
        return mob();
    }
}
