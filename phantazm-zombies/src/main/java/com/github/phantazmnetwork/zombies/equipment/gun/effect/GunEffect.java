package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.zombies.equipment.gun.Gun;
import org.jetbrains.annotations.NotNull;

public interface GunEffect extends Tickable, VariantSerializable {

    void accept(@NotNull Gun gun);

}
