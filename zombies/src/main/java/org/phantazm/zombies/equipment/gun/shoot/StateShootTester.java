package org.phantazm.zombies.equipment.gun.shoot;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.GunStats;
import org.phantazm.zombies.equipment.gun.GunUtils;
import org.phantazm.zombies.equipment.gun.reload.ReloadTester;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A {@link ShootTester} based solely on {@link GunState}.
 */
@Model("zombies.gun.shoot_tester.state")
@Cache(false)
public class StateShootTester implements ShootTester {

    private final GunStats stats;
    private final ReloadTester reloadTester;
    private final Supplier<Optional<? extends Entity>> entitySupplier;

    /**
     * Creates a {@link StateShootTester}.
     *
     * @param stats          The gun's {@link GunStats}
     * @param reloadTester   The gun's {@link ReloadTester}
     * @param entitySupplier A supplier which provides a "shooter", which may be absent
     */
    @FactoryMethod
    public StateShootTester(@NotNull @Child("stats") GunStats stats,
        @NotNull @Child("reloadTester") ReloadTester reloadTester,
        @NotNull Supplier<Optional<? extends Entity>> entitySupplier) {
        this.stats = Objects.requireNonNull(stats);
        this.reloadTester = Objects.requireNonNull(reloadTester);
        this.entitySupplier = Objects.requireNonNull(entitySupplier);
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
        return state.ticksSinceLastFire() * fireRateFactor() < stats.shotInterval();
    }

    @Override
    public boolean isShooting(@NotNull GunState state) {
        return state.ticksSinceLastShot() * fireRateFactor() < stats.shootSpeed();
    }

    private float fireRateFactor() {
        return this.entitySupplier.get().map(GunUtils::fireRateFactor).orElse(1F);

    }
}
