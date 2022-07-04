package com.github.phantazmnetwork.zombies.equipment.gun.reload;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.GunStats;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class StateReloadTester implements ReloadTester {

    public record Data(@NotNull Key statsKey) implements VariantSerializable {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.reload_tester.state");

        @Override
        public @NotNull Key getSerialKey() {
            return SERIAL_KEY;
        }
    }

    private final Data data;

    private final GunStats stats;

    public StateReloadTester(@NotNull Data data, @NotNull GunStats stats) {
        this.data = Objects.requireNonNull(data, "data");
        this.stats = Objects.requireNonNull(stats, "stats");
    }

    @Override
    public boolean shouldReload(@NotNull GunState state) {
        return canReload(state) && state.clip() != stats.maxClip();
    }

    @Override
    public boolean canReload(@NotNull GunState state) {
        return !isReloading(state) && state.ammo() > 0;
    }

    @Override
    public boolean isReloading(@NotNull GunState state) {
        return state.ticksSinceLastReload() < stats.reloadSpeed();
    }

    @Override
    public @NotNull VariantSerializable getData() {
        return data;
    }

}
