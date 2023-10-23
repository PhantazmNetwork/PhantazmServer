package org.phantazm.commons.flag;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public interface Flaggable {
    boolean hasFlag(@NotNull Key flag);

    void setFlag(@NotNull Key flag);

    void clearFlag(@NotNull Key flag);

    default boolean toggleFlag(@NotNull Key flag) {
        if (hasFlag(flag)) {
            clearFlag(flag);
            return false;
        } else {
            setFlag(flag);
            return true;
        }
    }

    @FunctionalInterface
    interface Source {
        @NotNull Flaggable flags();
    }
}
