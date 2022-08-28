package com.github.phantazmnetwork.zombies.game.map;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class BasicFlaggable implements Flaggable {
    private final Set<Key> flags;

    public BasicFlaggable() {
        this(16);
    }

    public BasicFlaggable(int initialSize) {
        this.flags = new HashSet<>(initialSize);
    }

    @Override
    public boolean hasFlag(@NotNull Key flag) {
        Objects.requireNonNull(flag, "flag");
        return flags.contains(flag);
    }

    @Override
    public void setFlag(@NotNull Key flag) {
        Objects.requireNonNull(flag, "flag");
        flags.add(flag);
    }

    @Override
    public void clearFlag(@NotNull Key flag) {
        Objects.requireNonNull(flag, "flag");
        flags.remove(flag);
    }
}
