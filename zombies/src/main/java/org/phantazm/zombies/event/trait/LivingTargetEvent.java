package org.phantazm.zombies.event.trait;

import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public interface LivingTargetEvent extends EntityTargetEvent {
    @NotNull LivingEntity target();
}
