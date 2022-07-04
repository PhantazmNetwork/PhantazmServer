package com.github.phantazmnetwork.zombies.equipment.gun.visual;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface GunStackMapper {

    @NotNull ItemStack map(@NotNull GunState state, @NotNull ItemStack intermediate);

    @NotNull VariantSerializable getData();

}
