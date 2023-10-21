package org.phantazm.zombies.equipment.gun.event;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.shoot.GunShot;

public record GunShootEvent(
    @NotNull Gun gun,
    @NotNull GunShot shot,
    @NotNull Entity entity) implements EntityInstanceEvent {

    @Override
    public @NotNull Entity getEntity() {
        return entity;
    }

}
