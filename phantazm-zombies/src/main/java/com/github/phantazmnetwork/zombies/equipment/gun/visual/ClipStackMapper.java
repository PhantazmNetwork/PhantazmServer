package com.github.phantazmnetwork.zombies.equipment.gun.visual;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.Gun;
import net.kyori.adventure.key.Key;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ClipStackMapper implements GunStackMapper {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.stack_mapper.clip.stack_count");

    @Override
    public @NotNull ItemStack map(@NotNull Gun gun, @NotNull ItemStack intermediate) {
        if (!gun.isReloading()) {
            return intermediate.withAmount(Math.max(1, gun.getState().clip()));
        }

        return intermediate;
    }

    @Override
    public @NotNull Key getSerialKey() {
        return SERIAL_KEY;
    }
}
