package org.phantazm.zombies.equipment.gun.event;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.shoot.GunShot;

/**
 * Event called after a gun has fired and the projectile (if any) has expired. It may or may not have hit any entities.
 *
 * @param gun    the gun that was used to shoot
 * @param shot   the {@link GunShot} that was fired
 * @param entity the shooter
 */
public record GunShootEvent(
    @NotNull Gun gun,
    @NotNull GunShot shot,
    @NotNull Entity entity) implements EntityInstanceEvent {

    @Override
    public @NotNull Entity getEntity() {
        return entity;
    }

}
