package org.phantazm.zombies.event.trait;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityEvent;
import org.jetbrains.annotations.NotNull;

public interface EntityTargetEvent extends EntityEvent {
    @NotNull Entity target();

    @Override
    default @NotNull Entity getEntity() {
        return target();
    }
}
