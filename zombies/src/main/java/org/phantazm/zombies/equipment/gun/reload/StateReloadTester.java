package org.phantazm.zombies.equipment.gun.reload;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
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
    private final PlayerView player;

    /**
     * Creates a {@link StateReloadTester}.
     *
     * @param stats The gun's {@link GunStats}
     */
    @FactoryMethod
    public StateReloadTester(@NotNull @Child("stats") GunStats stats, @NotNull PlayerView player) {
        this.stats = Objects.requireNonNull(stats);
        this.player = Objects.requireNonNull(player);
    }

    @Override
    public boolean shouldReload(@NotNull GunState state) {
        return canReload(state) && state.clip() != stats.maxClip(player.getPlayer().orElse(null))
            && state.clip() != state.ammo();
    }

    @Override
    public boolean canReload(@NotNull GunState state) {
        return !isReloading(state) && state.ammo() > 0 && state.reloadComplete();
    }

    @Override
    public boolean isReloading(@NotNull GunState state) {
        return state.ticksSinceLastReload() < stats.reloadSpeed(player.getPlayer().orElse(null));
    }
}
