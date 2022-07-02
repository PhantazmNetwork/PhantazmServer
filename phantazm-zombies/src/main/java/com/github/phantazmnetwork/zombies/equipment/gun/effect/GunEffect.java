package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.zombies.equipment.gun.Gun;
import org.jetbrains.annotations.NotNull;

public interface GunEffect extends VariantSerializable {

    void accept(@NotNull Gun gun);

}
