package org.phantazm.core.sound;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

public class BasicSongPlayer implements SongPlayer {
    private final Deque<SongImpl> songDeque;

    public BasicSongPlayer() {
        this.songDeque = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void tick(long time) {
        songDeque.removeIf(song -> song.tick(time));
    }

    @Override
    public @NotNull Song play(@NotNull Audience audience, @NotNull Sound.Source source, @NotNull Sound.Emitter emitter,
            @NotNull List<Note> notes, float volume, boolean loop) {
        Objects.requireNonNull(audience);
        Objects.requireNonNull(source);
        Objects.requireNonNull(emitter);
        Objects.requireNonNull(notes);

        SongImpl song = new SongImpl(audience, emitter, source, null, notes, volume, loop);
        if (song.nextNote != null) {
            songDeque.add(song);
        }

        return song;
    }

    @Override
    public @NotNull Song play(@NotNull Audience audience, @NotNull Sound.Source source, double x, double y, double z,
            @NotNull List<Note> notes, float volume, boolean loop) {
        Objects.requireNonNull(audience);
        Objects.requireNonNull(source);
        Objects.requireNonNull(notes);

        SongImpl song = new SongImpl(audience, null, source, new Vec(x, y, z), notes, volume, loop);
        if (song.nextNote != null) {
            songDeque.add(song);
        }

        return song;
    }

    private static class SongImpl implements Song {
        private final Audience audience;
        private final Sound.Emitter emitter;
        private final Sound.Source source;
        private final Point location;
        private final List<Note> notes;
        private final float volume;
        private final boolean loop;

        private boolean stopped;
        private Sound currentSound;

        private int noteIndex;
        private Note nextNote;
        private long lastNoteTicks;

        private SongImpl(Audience audience, Sound.Emitter emitter, Sound.Source source, Point location,
                List<Note> notes, float volume, boolean loop) {
            this.audience = Objects.requireNonNull(audience);
            this.emitter = emitter;
            this.source = source;
            this.location = location;
            this.notes = List.copyOf(notes);
            this.volume = volume;
            this.loop = loop;

            this.noteIndex = 0;
            this.nextNote = this.notes.isEmpty() ? null : this.notes.get(0);
            this.lastNoteTicks = -1;
        }

        @Override
        public void stop() {
            boolean isStopped = this.stopped;
            if (!isStopped) {
                this.stopped = true;
                Sound current = this.currentSound;
                if (current != null) {
                    audience.stopSound(current);
                }
            }
        }

        @Override
        public boolean isFinished() {
            return this.stopped;
        }

        private boolean tick(long time) {
            if (this.stopped) {
                return true;
            }

            Note nextNote = this.nextNote;
            if (nextNote == null) {
                return true;
            }

            long lastNoteTicks = ++this.lastNoteTicks;

            int nextNoteIndex;
            if (lastNoteTicks >= nextNote.ticks()) {
                this.lastNoteTicks = 0;

                do {
                    if (this.emitter != null) {
                        this.audience.playSound(
                                this.currentSound = Sound.sound(nextNote.soundType(), source, volume, nextNote.pitch()),
                                this.emitter);
                    }
                    else {
                        this.audience.playSound(
                                this.currentSound = Sound.sound(nextNote.soundType(), source, volume, nextNote.pitch()),
                                location.x(), location.y(), location.z());
                    }

                    nextNoteIndex = ++this.noteIndex;
                    if (nextNoteIndex < this.notes.size()) {
                        this.nextNote = nextNote = this.notes.get(nextNoteIndex);
                    }
                    else {
                        if (this.loop) {
                            this.noteIndex = -1;
                            this.nextNote = this.notes.get(0);
                            return false;
                        }

                        return true;
                    }
                } while (nextNote.ticks() <= 0);
            }

            return false;
        }
    }
}
