package org.phantazm.zombies.event.equipment;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.event.trait.EntityTargetEvent;
import org.phantazm.zombies.event.trait.GunEvent;

import java.util.Objects;

public class GunTargetSelectEvent implements EntityTargetEvent, GunEvent {
    private final Entity entity;
    private final Gun gun;

    private boolean forceSelect;

    public GunTargetSelectEvent(@NotNull Entity entity, @NotNull Gun gun) {
        this.entity = Objects.requireNonNull(entity);
        this.gun = Objects.requireNonNull(gun);
    }

    @Override
    public @NotNull Entity target() {
        return entity;
    }

    public @NotNull Gun gun() {
        return gun;
    }

    public void setForceSelect(boolean forceSelect) {
        this.forceSelect = forceSelect;
    }

    public boolean isForceSelected() {
        return this.forceSelect;
    }
}
