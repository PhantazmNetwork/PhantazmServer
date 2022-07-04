package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import org.jetbrains.annotations.NotNull;

public interface GunTickEffect {

    void tick(@NotNull GunState state, long time);

    @NotNull VariantSerializable getData();

}
