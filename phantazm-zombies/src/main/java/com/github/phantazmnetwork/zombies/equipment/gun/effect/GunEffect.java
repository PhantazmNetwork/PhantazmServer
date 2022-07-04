package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import org.jetbrains.annotations.NotNull;

public interface GunEffect extends GunTickEffect {

    void accept(@NotNull GunState state);

}
