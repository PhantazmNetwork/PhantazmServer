package com.github.phantazmnetwork.zombies.equipment.gun.visual;

import com.github.phantazmnetwork.zombies.equipment.gun.Gun;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ClipStackMapper implements GunStackMapper {
    @Override
    public @NotNull ItemStack map(@NotNull Gun gun, @NotNull ItemStack intermediate) {
        if (gun.canReload()) {
            return intermediate.withAmount(gun.getState().clip());
        }

        return intermediate;
    }
}
