package org.phantazm.zombies.equipment.perk.visual;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface PerkVisual {
    @NotNull ItemStack computeItemStack();

    boolean shouldCompute();
}
