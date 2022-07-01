package com.github.phantazmnetwork.zombies.equipment.gun.visual;

import com.github.phantazmnetwork.zombies.equipment.gun.Gun;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ReloadStackMapper implements GunStackMapper {
    @Override
    public @NotNull ItemStack map(@NotNull Gun gun, @NotNull ItemStack intermediate) {
        if (!gun.canReload()) {
            long reloadSpeed = gun.getLevel().reloadSpeed();
            int maxDamage = intermediate.material().registry().maxDamage();
            int damage = maxDamage - (int) (maxDamage * ((double) gun.getState().ticksSinceLastReload() / reloadSpeed));

            return intermediate.withMeta(builder -> builder.damage(damage));
        }

        return intermediate;
    }
}
