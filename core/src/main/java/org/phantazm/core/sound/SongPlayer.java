package org.phantazm.core.sound;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Tickable;

import java.util.List;

public interface SongPlayer extends Tickable {
    @NotNull Song play(@NotNull Audience audience, @NotNull Sound.Emitter emitter, @NotNull List<Note> notes,
            boolean loop);

    default @NotNull Song play(@NotNull Audience audience, @NotNull Sound.Emitter emitter, @NotNull List<Note> notes) {
        return play(audience, emitter, notes, false);
    }

    interface Song {
        void stop();
    }

    record Note(@NotNull Sound sound, int ticks) {
    }
}
