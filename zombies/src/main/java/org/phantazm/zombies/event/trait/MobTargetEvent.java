package org.phantazm.zombies.event.trait;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;

public interface MobTargetEvent extends LivingTargetEvent {
    @NotNull Mob target();
}
