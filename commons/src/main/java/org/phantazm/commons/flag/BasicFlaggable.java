package org.phantazm.commons.flag;

import it.unimi.dsi.fastutil.Hash;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BasicFlaggable implements Flaggable {
    private final Set<Key> flags;

    public BasicFlaggable() {
        this(Hash.DEFAULT_INITIAL_SIZE, Hash.DEFAULT_LOAD_FACTOR);
    }

    public BasicFlaggable(int initialSize) {
        this(initialSize, Hash.DEFAULT_LOAD_FACTOR);
    }

    public BasicFlaggable(int initialSize, float loadFactor) {
        this.flags = Collections.newSetFromMap(new ConcurrentHashMap<>(initialSize, loadFactor));
    }

    @Override
    public boolean hasFlag(@NotNull Key flag) {
        Objects.requireNonNull(flag);
        return flags.contains(flag);
    }

    @Override
    public void setFlag(@NotNull Key flag) {
        Objects.requireNonNull(flag);
        flags.add(flag);
    }

    @Override
    public void clearFlag(@NotNull Key flag) {
        Objects.requireNonNull(flag);
        flags.remove(flag);
    }
}
