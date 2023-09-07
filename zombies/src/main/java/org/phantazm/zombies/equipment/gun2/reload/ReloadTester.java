package org.phantazm.zombies.equipment.gun2.reload;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun2.GunState;
import org.phantazm.zombies.equipment.gun2.GunStats;

import java.util.Objects;

public class ReloadTester {

    private final GunStats stats;

    private final GunState state;

    public ReloadTester(@NotNull GunStats stats, @NotNull GunState state) {
        this.stats = Objects.requireNonNull(stats);
        this.state = Objects.requireNonNull(state);
    }

    public boolean isReloading() {
        return state.getTicksSinceLastReload() < stats.reloadSpeed();
    }

    public boolean shouldReload() {
        return canReload() && state.getClip() != stats.maxClip() && state.getClip() != state.getAmmo();
    }

    public boolean canReload() {
        return !isReloading() && state.getAmmo() > 0 && state.isReloadComplete();
    }

}
