package org.phantazm.mob2.selector;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;

public interface Selector {
    @NotNull
    Target select(@NotNull Mob mob);
}
