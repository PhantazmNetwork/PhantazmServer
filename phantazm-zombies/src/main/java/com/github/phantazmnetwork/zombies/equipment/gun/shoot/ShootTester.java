package com.github.phantazmnetwork.zombies.equipment.gun.shoot;

import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import org.jetbrains.annotations.NotNull;

public interface ShootTester {
    boolean shouldShoot(@NotNull GunState state);

    boolean canFire(@NotNull GunState state);

    boolean isFiring(@NotNull GunState state);

    boolean isShooting(@NotNull GunState state);

}
