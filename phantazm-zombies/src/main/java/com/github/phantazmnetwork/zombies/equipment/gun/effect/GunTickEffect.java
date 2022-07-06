package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface GunTickEffect {

    void tick(@NotNull GunState state, long time);

}
