package org.phantazm.zombies.equipment.gun2.event;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class GunLoseAmmoEvent implements Event {

    private final UUID gunUUID;

    private int ammoLoss;

    public GunLoseAmmoEvent(@NotNull UUID gunUUID, int ammoLoss) {
        this.gunUUID = Objects.requireNonNull(gunUUID);
        this.ammoLoss = ammoLoss;
    }

    public @NotNull UUID getGunUUID() {
        return gunUUID;
    }

    public int getAmmoLoss() {
        return ammoLoss;
    }

    public void setAmmoLoss(int ammoLoss) {
        this.ammoLoss = ammoLoss;
    }
}
