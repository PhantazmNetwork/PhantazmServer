package com.github.phantazmnetwork.zombies.equipment.gun.reload;

import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import org.jetbrains.annotations.NotNull;

public interface ReloadTester {

    boolean shouldReload(@NotNull GunState state);

    boolean canReload(@NotNull GunState state);

    boolean isReloading(@NotNull GunState state);

}
