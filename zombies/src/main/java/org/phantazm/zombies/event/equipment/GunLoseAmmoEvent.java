package org.phantazm.zombies.event.equipment;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.Gun;

public class GunLoseAmmoEvent implements EntityInstanceEvent {
    private final Entity entity;
    private final Gun gun;

    private int ammoLost;

    public GunLoseAmmoEvent(@NotNull Entity entity, @NotNull Gun gun, int ammoLost) {
        this.entity = entity;
        this.gun = gun;
        this.ammoLost = ammoLost;
    }

    @Override
    public @NotNull Entity getEntity() {
        return entity;
    }

    public @NotNull Gun getGun() {
        return gun;
    }

    public int getAmmoLost() {
        return ammoLost;
    }

    public void setAmmoLost(int ammoLost) {
        this.ammoLost = ammoLost;
    }
}
