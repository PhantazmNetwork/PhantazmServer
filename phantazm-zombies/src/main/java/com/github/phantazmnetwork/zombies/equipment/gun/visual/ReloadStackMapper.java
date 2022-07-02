package com.github.phantazmnetwork.zombies.equipment.gun.visual;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.Gun;
import net.kyori.adventure.key.Key;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ReloadStackMapper implements GunStackMapper {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.stack_mapper.reload.durability");

    @Override
    public @NotNull ItemStack map(@NotNull Gun gun, @NotNull ItemStack intermediate) {
        if (gun.isReloading()) {
            long reloadSpeed = gun.getLevel().reloadSpeed();
            int maxDamage = intermediate.material().registry().maxDamage();
            int damage = maxDamage - (int) (maxDamage * ((double) gun.getState().ticksSinceLastReload() / reloadSpeed));

            return intermediate.withMeta(builder -> builder.damage(damage));
        }

        return intermediate;
    }

    @Override
    public @NotNull Key getSerialKey() {
        return SERIAL_KEY;
    }
}
