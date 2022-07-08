package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import org.jetbrains.annotations.NotNull;

/**
 * An effect which a gun may apply.
 */
public interface GunEffect extends GunTickEffect {

    /**
     * Applies the gun effect.
     * @param state The current {@link GunState} of the gun
     */
    void apply(@NotNull GunState state);

}
