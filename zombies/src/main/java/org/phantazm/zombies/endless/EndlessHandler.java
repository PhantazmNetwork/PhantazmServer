package org.phantazm.zombies.endless;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Round;

public interface EndlessHandler {
    @NotNull Round generateRound(int round);
}
