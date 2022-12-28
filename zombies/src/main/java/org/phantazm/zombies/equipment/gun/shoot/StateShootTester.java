package org.phantazm.zombies.equipment.gun.shoot;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.GunStats;
import org.phantazm.zombies.equipment.gun.reload.ReloadTester;

import java.util.Objects;

/**
 * A {@link ShootTester} based solely on {@link GunState}.
 */
@Model("zombies.gun.shoot_tester.state")
public class StateShootTester implements ShootTester {

    private final GunStats stats;
    private final ReloadTester reloadTester;

    /**
     * Creates a {@link StateShootTester}.
     *
     * @param stats        The gun's {@link GunStats}
     * @param reloadTester The gun's {@link ReloadTester}
     */
    @FactoryMethod
    public StateShootTester(@NotNull @Child("stats") GunStats stats,
            @NotNull @Child("reload_tester") ReloadTester reloadTester) {
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

    /**
     * Data for a {@link StateShootTester}.
     *
     * @param statsPath        A path to the gun's {@link GunStats}
     * @param reloadTesterPath A path to the gun's {@link ReloadTester}
     */
    @DataObject
    public record Data(@NotNull @DataPath("stats") String statsPath,
                       @NotNull @DataPath("reload_tester") String reloadTesterPath) {

        /**
         * Creates a {@link Data}.
         *
         * @param statsPath        A path to the gun's {@link GunStats}
         * @param reloadTesterPath A path to the gun's {@link ReloadTester}
         */
        public Data {
            Objects.requireNonNull(statsPath, "statsPath");
            Objects.requireNonNull(reloadTesterPath, "reloadTesterPath");
        }

    }

}
