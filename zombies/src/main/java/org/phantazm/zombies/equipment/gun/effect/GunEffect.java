package org.phantazm.zombies.equipment.gun.effect;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.GunState;

/**
 * An effect which a gun may apply.
 */
public interface GunEffect extends GunTickEffect {

    /**
     * Applies the gun effect.
     *
     * @param state The current {@link GunState} of the gun
     */
    void apply(@NotNull GunState state);

}
