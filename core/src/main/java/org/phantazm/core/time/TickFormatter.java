package org.phantazm.core.time;

import org.jetbrains.annotations.NotNull;

public interface TickFormatter {

    @NotNull String format(long ticks);

}
