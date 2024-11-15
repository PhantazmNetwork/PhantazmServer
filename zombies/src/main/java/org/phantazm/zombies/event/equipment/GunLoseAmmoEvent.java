package org.phantazm.zombies.event.equipment;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.event.trait.GunEvent;

public class GunLoseAmmoEvent implements EntityInstanceEvent, GunEvent {
    private final Entity entity;
    private final Gun gun;
    private final int oldAmmoCount;

    private int ammoLost;

    public GunLoseAmmoEvent(@NotNull Entity entity, @NotNull Gun gun, int oldAmmoCount, int ammoLost) {
        this.entity = entity;
        this.gun = gun;
        this.oldAmmoCount = oldAmmoCount;
        this.ammoLost = ammoLost;
    }

    @Override
    public @NotNull Entity getEntity() {
        return entity;
    }

    @Override
    public @NotNull Gun gun() {
        return gun;
    }

    public int oldAmmoCount() {
        return oldAmmoCount;
    }

    public int getAmmoLost() {
        return ammoLost;
    }

    public void setAmmoLost(int ammoLost) {
        this.ammoLost = ammoLost;
    }
}
