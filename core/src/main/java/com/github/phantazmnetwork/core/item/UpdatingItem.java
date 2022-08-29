package com.github.phantazmnetwork.core.item;

import com.github.phantazmnetwork.core.gui.ItemUpdater;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface UpdatingItem extends ItemUpdater {
    @NotNull ItemStack currentItem();
}
