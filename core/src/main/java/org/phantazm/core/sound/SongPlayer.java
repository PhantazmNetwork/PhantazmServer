package org.phantazm.core.sound;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Tickable;

import java.util.List;

public interface SongPlayer extends Tickable {
    @NotNull Song play(@NotNull Audience audience, @NotNull Sound.Source source, @NotNull Sound.Emitter emitter,
        @NotNull List<Note> notes, float volume, boolean loop);

    @NotNull Song play(@NotNull Audience audience, @NotNull Sound.Source source, double x, double y, double z,
        @NotNull List<Note> notes, float volume, boolean loop);

    default @NotNull Song play(@NotNull Audience audience, @NotNull Sound.Source source, double x, double y, double z,
        @NotNull List<Note> notes, float volume) {
        return play(audience, source, x, y, z, notes, volume, false);
    }

    default @NotNull Song play(@NotNull Audience audience, @NotNull Sound.Source source, @NotNull Point point,
        @NotNull List<Note> notes, float volume) {
        return play(audience, source, point.x(), point.y(), point.z(), notes, volume);
    }

    default @NotNull Song play(@NotNull Audience audience, @NotNull Sound.Source source, @NotNull Sound.Emitter emitter,
        @NotNull List<Note> notes, float volume) {
        return play(audience, source, emitter, notes, volume, false);
    }

    default @NotNull Song play(@NotNull Audience audience, @NotNull Sound.Source source, @NotNull Point point,
        @NotNull List<Note> notes, float volume, boolean loop) {
        return play(audience, source, point.x(), point.y(), point.z(), notes, volume, loop);
    }

    interface Song {
        void stop();

        boolean isFinished();
    }

    record Note(@NotNull Key soundType,
        float pitch,
        int ticks) {
    }
}
