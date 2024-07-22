package org.phantazm.zombies.event.trait;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

public interface LivingTargetEvent extends Event {
    @NotNull LivingEntity target();
}
