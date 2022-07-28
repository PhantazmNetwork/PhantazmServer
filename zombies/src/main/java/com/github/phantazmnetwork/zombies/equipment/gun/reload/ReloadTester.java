package com.github.phantazmnetwork.zombies.equipment.gun.reload;

import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import org.jetbrains.annotations.NotNull;

/**
 * Tests for a gun's ability to reload.
 */
public interface ReloadTester {

    /**
     * Tests if the gun should reload. While a gun may be able to reload as determined by {@link #canReload(GunState)},
     * it may be better for it not to, like if its clip is full.
     *
     * @param state The gun's current {@link GunState}
     * @return Whether the gun should reload
     */
    boolean shouldReload(@NotNull GunState state);

    /**
     * Tests if the gun can reload. This is based on purely physical constraints, like ammo remaining.
     *
     * @param state The gun's current {@link GunState}
     * @return Whether the gun can reload
     */
    boolean canReload(@NotNull GunState state);

    /**
     * Tests if the gun is currently reloading.
     *
     * @param state The gun's current {@link GunState}
     * @return Whether the gun is currently reloading
     */
    boolean isReloading(@NotNull GunState state);

}
