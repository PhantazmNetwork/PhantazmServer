package com.github.phantazmnetwork.zombies.equipment.gun.shoot;

import net.kyori.adventure.key.Keyed;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.GunStats;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.ReloadTester;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class StateShootTester implements ShootTester {

    public record Data(@NotNull Key statsKey, @NotNull Key reloadTesterKey) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.shoot_tester.state");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    private final Data data;

    private final GunStats stats;

    private final ReloadTester reloadTester;

    public StateShootTester(@NotNull Data data, @NotNull GunStats stats, @NotNull ReloadTester reloadTester) {
        this.data = Objects.requireNonNull(data, "data");
        this.stats = Objects.requireNonNull(stats, "stats");
        this.reloadTester = Objects.requireNonNull(reloadTester, "reloadTester");
    }

    @Override
    public boolean shouldShoot(@NotNull GunState state) {
        return !isShooting(state) && canFire(state) && state.queuedShots() == 0;
    }

    @Override
    public boolean canFire(@NotNull GunState state) {
        return !isFiring(state) && state.ammo() > 0 && reloadTester.canReload(state);
    }

    @Override
    public boolean isFiring(@NotNull GunState state) {
        return state.ticksSinceLastFire() < stats.shotInterval();
    }

    @Override
    public boolean isShooting(@NotNull GunState state) {
        return state.ticksSinceLastShot() < stats.shootSpeed();
    }

    @Override
    public @NotNull Keyed getData() {
        return data;
    }

}
