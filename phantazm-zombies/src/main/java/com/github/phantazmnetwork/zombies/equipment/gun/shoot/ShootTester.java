package com.github.phantazmnetwork.zombies.equipment.gun.shoot;

import net.kyori.adventure.key.Keyed;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import org.jetbrains.annotations.NotNull;

public interface ShootTester {
    boolean shouldShoot(@NotNull GunState state);

    boolean canFire(@NotNull GunState state);

    boolean isShooting(@NotNull GunState state);

    @NotNull Keyed getData();
}
