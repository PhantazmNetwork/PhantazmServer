package org.phantazm.zombies.event.trait;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface ShooterEvent extends EntityTargetEvent {
    @NotNull Entity shooter();

    @Override
    default @NotNull Entity target() {
        return shooter();
    }
}
