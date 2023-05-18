package org.phantazm.zombies.equipment.gun.reload;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.GunStats;

import java.util.Objects;

/**
 * A {@link ReloadTester} based solely on {@link GunState}.
 */
@Model("zombies.gun.reload_tester.state")
@Cache(false)
public class StateReloadTester implements ReloadTester {

    private final GunStats stats;

    /**
     * Creates a {@link StateReloadTester}.
     *
     * @param stats The gun's {@link GunStats}
     */
    @FactoryMethod
    public StateReloadTester(@NotNull @Child("stats") GunStats stats) {
        this.stats = Objects.requireNonNull(stats, "stats");
    }

    @Override
    public boolean shouldReload(@NotNull GunState state) {
        return canReload(state) && state.clip() != stats.maxClip() && state.clip() != state.ammo();
    }

    @Override
    public boolean canReload(@NotNull GunState state) {
        return !isReloading(state) && state.ammo() > 0;
    }

    @Override
    public boolean isReloading(@NotNull GunState state) {
        return state.ticksSinceLastReload() < stats.reloadSpeed();
    }

    /**
     * Data for a {@link StateReloadTester}.
     *
     * @param stats A path to the gun's {@link GunStats}
     */
    @DataObject
    public record Data(@NotNull @ChildPath("stats") String stats) {
    }
}
