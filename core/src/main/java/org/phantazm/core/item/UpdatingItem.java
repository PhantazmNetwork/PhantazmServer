package org.phantazm.core.item;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.gui.ItemUpdater;

public interface UpdatingItem extends ItemUpdater {
    @NotNull ItemStack currentItem();
}
