package org.phantazm.core.sound;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Tickable;

import java.util.List;

public interface SongPlayer extends Tickable {
    @NotNull Song play(@NotNull Audience audience, @NotNull Sound.Emitter emitter, @NotNull List<Note> notes,
            boolean loop);

    @NotNull Song play(@NotNull Audience audience, double x, double y, double z, @NotNull List<Note> notes,
            boolean loop);

    default @NotNull Song play(@NotNull Audience audience, double x, double y, double z, @NotNull List<Note> notes) {
        return play(audience, x, y, z, notes, false);
    }

    default @NotNull Song play(@NotNull Audience audience, @NotNull Point point, @NotNull List<Note> notes) {
        return play(audience, point.x(), point.y(), point.z(), notes);
    }

    default @NotNull Song play(@NotNull Audience audience, @NotNull Sound.Emitter emitter, @NotNull List<Note> notes) {
        return play(audience, emitter, notes, false);
    }

    default @NotNull Song play(@NotNull Audience audience, @NotNull Point point, @NotNull List<Note> notes,
            boolean loop) {
        return play(audience, point.x(), point.y(), point.z(), notes, loop);
    }

    interface Song {
        void stop();

        boolean isFinished();
    }

    record Note(@NotNull Sound sound, int ticks) {
    }
}
