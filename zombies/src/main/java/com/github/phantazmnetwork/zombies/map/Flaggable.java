package com.github.phantazmnetwork.zombies.map;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public interface Flaggable {
    boolean hasFlag(@NotNull Key flag);

    void setFlag(@NotNull Key flag);

    void clearFlag(@NotNull Key flag);

    default void toggleFlag(@NotNull Key flag) {
        if (hasFlag(flag)) {
            clearFlag(flag);
        }
        else {
            setFlag(flag);
        }
    }

    @FunctionalInterface
    interface Source {
        @NotNull Flaggable flags();
    }
}
