package com.github.phantazmnetwork.zombies.equipment.gun.visual;

import com.github.phantazmnetwork.zombies.equipment.gun.Gun;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface GunStackMapper {

    @NotNull ItemStack map(@NotNull Gun gun, @NotNull ItemStack intermediate);

}
