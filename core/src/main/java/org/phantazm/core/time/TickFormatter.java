package org.phantazm.core.time;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface TickFormatter {

    @NotNull Component format(long ticks);

}
