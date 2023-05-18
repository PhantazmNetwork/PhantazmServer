package org.phantazm.mob;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityEvent;
import org.jetbrains.annotations.NotNull;

public interface PhantazmMobEvent extends EntityEvent {
    @NotNull PhantazmMob getPhantazmMob();

    @Override
    default @NotNull Entity getEntity() {
        return getPhantazmMob().entity();
    }
}
