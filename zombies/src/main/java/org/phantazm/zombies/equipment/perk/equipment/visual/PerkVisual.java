package org.phantazm.zombies.equipment.perk.equipment.visual;

import net.minestom.server.Tickable;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface PerkVisual extends Tickable {
    @NotNull
    ItemStack computeItemStack();

    boolean shouldCompute();

    void leftClick(boolean success);

    void rightClick(boolean success);
}
