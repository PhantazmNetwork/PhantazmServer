package org.phantazm.zombies.endless;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Round;

public interface Endless {
    @NotNull Round generateRound(int roundIndex);
}
