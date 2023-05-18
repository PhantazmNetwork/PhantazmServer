package org.phantazm.core.sound;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Set;

public interface SongLoader {
    void load();

    @NotNull List<SongPlayer.Note> getNotes(@NotNull Key song);

    @NotNull @Unmodifiable Set<Key> songs();
}
