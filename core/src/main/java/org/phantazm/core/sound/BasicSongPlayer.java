package org.phantazm.core.sound;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
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
    public @NotNull Song play(@NotNull Audience audience, @NotNull Sound.Emitter emitter, @NotNull List<Note> notes,
            boolean loop) {
        SongImpl song = new SongImpl(audience, emitter, notes, loop);
        if (song.nextNote != null) {
            songDeque.add(song);
        }

        return song;
    }

    private static class SongImpl implements Song {
        private final Audience audience;
        private final Sound.Emitter emitter;
        private final List<Note> notes;
        private final boolean loop;

        private boolean stopped;
        private Sound currentSound;

        private int noteIndex;
        private Note nextNote;
        private long lastNoteTime;

        private SongImpl(Audience audience, Sound.Emitter emitter, List<Note> notes, boolean loop) {
            this.audience = Objects.requireNonNull(audience, "audience");
            this.emitter = Objects.requireNonNull(emitter, "emitter");
            this.notes = List.copyOf(notes);
            this.loop = loop;

            this.noteIndex = 0;
            this.nextNote = this.notes.isEmpty() ? null : this.notes.get(0);
            this.lastNoteTime = -1;
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

        private boolean tick(long time) {
            if (this.stopped) {
                return true;
            }

            Note nextNote = this.nextNote;
            if (nextNote == null) {
                return true;
            }

            long lastNoteTime = this.lastNoteTime;
            if (lastNoteTime == -1) {
                this.lastNoteTime = time;
                lastNoteTime = time;
            }

            int ticksSinceLastNote = (int)((time - lastNoteTime) / MinecraftServer.TICK_MS);

            int nextNoteIndex;
            if (ticksSinceLastNote >= nextNote.ticks()) {
                this.lastNoteTime = time;

                do {
                    this.audience.playSound(this.currentSound = nextNote.sound(), this.emitter);

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
