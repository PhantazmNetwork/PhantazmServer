package org.phantazm.zombies.equipment.gun2.visual;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface GunStackMapper {

    @NotNull ItemStack apply(@NotNull ItemStack stack);

}
