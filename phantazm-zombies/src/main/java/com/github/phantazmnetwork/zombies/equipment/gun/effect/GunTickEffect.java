package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import org.jetbrains.annotations.NotNull;

/**
 * An effect which guns may tick.
 */
@FunctionalInterface
public interface GunTickEffect {

    /**
     * Ticks the effect.
     * @param state The current {@link GunState} of the gun
     * @param time The time of the tick
     */
    void tick(@NotNull GunState state, long time);

}
