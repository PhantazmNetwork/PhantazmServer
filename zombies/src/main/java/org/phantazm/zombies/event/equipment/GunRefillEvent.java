package org.phantazm.zombies.event.equipment;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.event.trait.GunEvent;

public class GunRefillEvent implements EntityInstanceEvent, GunEvent {
    private final Entity entity;
    private final Gun gun;

    public GunRefillEvent(@NotNull Entity entity, @NotNull Gun gun) {
        this.entity = entity;
        this.gun = gun;
    }

    @Override
    public @NotNull Entity getEntity() {
        return entity;
    }

    @Override
    public @NotNull Gun gun() {
        return gun;
    }
}
