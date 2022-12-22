package org.phantazm.zombies.equipment.gun.visual;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.GunState;

/**
 * Maps an {@link ItemStack} based on a gun's {@link GunState}.
 */
@FunctionalInterface
public interface GunStackMapper {

    /**
     * Maps an {@link ItemStack} based on a gun's {@link GunState}.
     *
     * @param state        The state of the gun
     * @param intermediate The current representation of the gun's {@link ItemStack}
     * @return The mapped {@link ItemStack}
     */
    @NotNull ItemStack map(@NotNull GunState state, @NotNull ItemStack intermediate);

}
