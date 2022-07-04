package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public interface GunTickEffect {

    void tick(@NotNull GunState state, long time);

    @NotNull Keyed getData();

}
