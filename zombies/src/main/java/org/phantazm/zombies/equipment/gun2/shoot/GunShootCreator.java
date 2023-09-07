package org.phantazm.zombies.equipment.gun2.shoot;

import net.minestom.server.event.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun2.GunState;
import org.phantazm.zombies.equipment.gun2.GunStats;
import org.phantazm.zombies.equipment.gun2.event.GunEmptyEvent;
import org.phantazm.zombies.equipment.gun2.event.GunFireEvent;
import org.phantazm.zombies.equipment.gun2.event.GunLoseAmmoEvent;
import org.phantazm.zombies.equipment.gun2.reload.GunReload;

import java.util.Objects;
import java.util.UUID;

public class GunShootCreator implements GunShoot {

    private final UUID gunUUID;

    private final ShootTester shootTester;

    private final GunReload reload;

    private final GunStats stats;

    private final GunState state;

    public GunShootCreator(@NotNull UUID gunUUID, @NotNull ShootTester shootTester, @NotNull GunReload reload, @NotNull GunStats stats, @NotNull GunState state) {
        this.gunUUID = Objects.requireNonNull(gunUUID);
        this.shootTester = Objects.requireNonNull(shootTester);
        this.reload = Objects.requireNonNull(reload);
        this.stats = Objects.requireNonNull(stats);
        this.state = Objects.requireNonNull(state);
    }

    @Override
    public void shoot() {
        if (shootTester.shouldShoot()) {
            state.setQueuedShots(stats.shots() - 1);
            state.setTicksSinceLastShot(0);
        }
    }

    @Override
    public void fire() {
        GunLoseAmmoEvent event = new GunLoseAmmoEvent(gunUUID, 1);
        EventDispatcher.call(event);

        int ammoLoss = event.getAmmoLoss();
        state.setTicksSinceLastFire(0L);
        state.setAmmo(state.getAmmo() - ammoLoss);
        state.setClip(state.getClip() - ammoLoss);

        if (state.getClip() == 0) {
            if (state.getAmmo() > 0) {
                reload.reload();
            } else {
                EventDispatcher.call(new GunEmptyEvent(gunUUID));
            }
        }

        EventDispatcher.call(new GunFireEvent(gunUUID));
    }

}
