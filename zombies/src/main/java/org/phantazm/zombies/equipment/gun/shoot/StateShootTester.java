package org.phantazm.zombies.equipment.gun.shoot;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.GunStats;
import org.phantazm.zombies.equipment.gun.reload.ReloadTester;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A {@link ShootTester} based solely on {@link GunState}.
 */
@Model("zombies.gun.shoot_tester.state")
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
            @NotNull @Child("reload_tester") ReloadTester reloadTester,
            @NotNull Supplier<Optional<? extends Entity>> entitySupplier) {
        this.stats = Objects.requireNonNull(stats, "stats");
        this.reloadTester = Objects.requireNonNull(reloadTester, "reloadTester");
        this.entitySupplier = Objects.requireNonNull(entitySupplier, "entitySupplier");
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
        Optional<? extends Entity> shooter = this.entitySupplier.get();

        float factor = 1F;
        if (shooter.isPresent() && shooter.get() instanceof LivingEntity livingEntity) {
            factor = livingEntity.getAttributeValue(Attributes.FIRE_RATE_MULTIPLIER);
        }

        return factor;
    }

    /**
     * Data for a {@link StateShootTester}.
     *
     * @param stats        A path to the gun's {@link GunStats}
     * @param reloadTester A path to the gun's {@link ReloadTester}
     */
    @DataObject
    public record Data(@NotNull @ChildPath("stats") String stats,
                       @NotNull @ChildPath("reload_tester") String reloadTester) {

        /**
         * Creates a {@link Data}.
         *
         * @param stats        A path to the gun's {@link GunStats}
         * @param reloadTester A path to the gun's {@link ReloadTester}
         */
        public Data {
            Objects.requireNonNull(stats, "stats");
            Objects.requireNonNull(reloadTester, "reloadTester");
        }

    }

}
