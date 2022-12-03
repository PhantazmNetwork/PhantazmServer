package com.github.phantazmnetwork.zombies.map;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class BasicFlaggable implements Flaggable {
    private final Set<Key> flags;

    public BasicFlaggable() {
        this(Hash.DEFAULT_INITIAL_SIZE, Hash.DEFAULT_LOAD_FACTOR);
    }

    public BasicFlaggable(int initialSize) {
        this(initialSize, Hash.DEFAULT_LOAD_FACTOR);
    }

    public BasicFlaggable(int initialSize, float loadFactor) {
        this.flags = new HashSet<>(initialSize, loadFactor);
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
