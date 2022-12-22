package org.phantazm.core.item;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record ItemAnimationFrame(@NotNull ItemStack itemStack, int delayTicks) {

}
