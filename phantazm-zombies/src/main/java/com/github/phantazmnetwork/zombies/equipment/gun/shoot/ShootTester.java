package com.github.phantazmnetwork.zombies.equipment.gun.shoot;

import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import org.jetbrains.annotations.NotNull;

/**
 * Tests for a gun's ability to shoot.
 */
public interface ShootTester {

    /**
     * Tests if the gun should shoot.
     * A gun may be able to fire as determined by {@link #canFire(GunState)} while not being able to shoot.
     * For example, a gun may fire multiple times every single time it shoots. However, while the gun is still sending a volley of shots,
     * while it may be able to fire, it cannot start an entirely new volley of shots.
     * @param state The gun's current {@link GunState}
     * @return Whether the gun should shoot
     */
    boolean shouldShoot(@NotNull GunState state);

    /**
     * Tests if the gun can fire.
     * @param state The gun's current {@link GunState}
     * @return Whether the gun can fire
     */
    boolean canFire(@NotNull GunState state);

    /**
     * Tests if the gun is currently firing.
     * @param state The gun's current {@link GunState}
     * @return Whether the gun is currently firing
     */
    boolean isFiring(@NotNull GunState state);

    /**
     * Tests if the gun is currently shooting.
     * @param state The gun's current {@link GunState}
     * @return Whether the gun is currently shooting
     */
    boolean isShooting(@NotNull GunState state);

}
