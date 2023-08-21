package org.phantazm.mob2;

import net.minestom.server.event.trait.EntityEvent;
import org.jetbrains.annotations.NotNull;

public interface MobEvent extends EntityEvent {
    @NotNull Mob mob();
}
