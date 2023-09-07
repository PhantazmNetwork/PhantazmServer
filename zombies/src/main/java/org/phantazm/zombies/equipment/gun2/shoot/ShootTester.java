package org.phantazm.zombies.equipment.gun2.shoot;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun2.GunState;
import org.phantazm.zombies.equipment.gun2.GunStats;
import org.phantazm.zombies.equipment.gun2.reload.ReloadTester;

import java.util.Objects;
import java.util.function.LongSupplier;

public class ShootTester {

    private final ReloadTester reloadTester;

    private final GunStats stats;

    private final GunState state;

    private final LongSupplier fireRateFactor;

    public ShootTester(@NotNull ReloadTester reloadTester, @NotNull GunStats stats, @NotNull GunState state, @NotNull LongSupplier fireRateFactor) {
        this.reloadTester = Objects.requireNonNull(reloadTester);
        this.stats = Objects.requireNonNull(stats);
        this.state = Objects.requireNonNull(state);
        this.fireRateFactor = Objects.requireNonNull(fireRateFactor);
    }

    public boolean shouldShoot() {
        return !isShooting() && canFire() && state.getQueuedShots() == 0;
    }

    public boolean canFire() {
        return !isFiring() && state.getAmmo() > 0 && reloadTester.canReload();
    }

    public boolean isFiring() {
        return state.getTicksSinceLastFire() * fireRateFactor.getAsLong() < stats.shotInterval();
    }

    public boolean isShooting() {
        return state.getTicksSinceLastShot() * fireRateFactor.getAsLong() < stats.shootSpeed();
    }

}
