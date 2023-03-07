package org.phantazm.core.sound;

import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.bridge.Configuration;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class NBSSongLoader implements SongLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(NBSSongLoader.class);

    private static final int MIN_KEY = 33;
    private static final int MAX_KEY = 57;

    private static final DefaultInstruments[] VALUES = DefaultInstruments.values();

    private final Path rootPath;
    private final ConfigCodec codec;

    private Map<Key, List<SongPlayer.Note>> map;

    public NBSSongLoader(@NotNull Path rootPath, @NotNull ConfigCodec codec) {
        this.rootPath = Objects.requireNonNull(rootPath, "rootPath");
        this.codec = Objects.requireNonNull(codec, "codec");
    }

    private static float normalizeKey(int key) {
        int normalized = MathUtils.clamp(key, MIN_KEY, MAX_KEY);
        int uses = normalized - MIN_KEY;

        double pow = Math.pow(2, (uses - 12) / 12D);
        return (float)MathUtils.clamp(pow, 0, 2);
    }

    @Override
    public void load() {
        Map<Key, List<SongPlayer.Note>> map = new HashMap<>();
        try {
            Files.createDirectories(rootPath);
        }
        catch (IOException e) {
            LOGGER.warn("Error creating song directory");
        }

        LOGGER.info("Loading songs...");
        try (Stream<Path> pathStream = Files.list(rootPath)) {
            pathStream.forEach(path -> {
                if (path.getFileName().toString().endsWith(".json")) {
                    try {
                        ConfigElement element = Configuration.read(path, codec);

                        ConfigList customInstruments = element.getListOrThrow("custom_instruments").asList();

                        List<Key> customInstrumentNames = new ArrayList<>(customInstruments.size());
                        for (ConfigElement customInstrument : customInstruments) {
                            @Subst("a")
                            String name = customInstrument.getStringOrThrow("instrument_name");
                            customInstrumentNames.add(Key.key(name));
                        }

                        @Subst("a")
                        String songName = element.getStringOrThrow("song_name");
                        Key songKey = Key.key(songName);

                        ConfigList list = element.getListOrThrow("notes");
                        List<SongPlayer.Note> notes = new ArrayList<>(list.size());
                        for (ConfigElement note : list) {
                            ConfigNode noteNode = note.asNode();

                            int delay = noteNode.getNumberOrThrow("delay_ticks").intValue();
                            int instrument = noteNode.getNumberOrThrow("note_block_instrument").intValue();
                            int key = noteNode.getNumberOrThrow("note_block_key").intValue();

                            Key instrumentName;
                            if (instrument >= 0 && instrument < VALUES.length) {
                                instrumentName = VALUES[instrument].key;
                            }
                            else {
                                instrumentName = customInstrumentNames.get(instrument - VALUES.length);
                            }

                            Sound sound = Sound.sound(instrumentName, Sound.Source.MUSIC, 10, normalizeKey(key));
                            SongPlayer.Note actualNote = new SongPlayer.Note(sound, delay);
                            notes.add(actualNote);
                        }

                        map.put(songKey, List.copyOf(notes));
                        LOGGER.info("Loaded song " + songKey);
                    }
                    catch (Exception e) {
                        LOGGER.warn("Exception reading file", e);
                    }
                }
            });
        }
        catch (IOException e) {
            LOGGER.warn("Exception listing files", e);
        }

        LOGGER.info("Loaded " + map.size() + " songs.");
        this.map = Map.copyOf(map);
    }

    @Override
    public @NotNull List<SongPlayer.Note> getNotes(@NotNull Key song) {
        List<SongPlayer.Note> notes = map.get(song);
        if (notes == null) {
            throw new IllegalArgumentException("Song does not exist");
        }

        return notes;
    }

    @Override
    public @NotNull @Unmodifiable Set<Key> songs() {
        return map.keySet();
    }

    private enum DefaultInstruments {
        PIANO(Key.key("block.note_block.harp")),
        DOUBLE_BASS(Key.key("block.note_block.bass")),
        BASS_DRUM(Key.key("block.note_block.basedrum")),
        SNARE_DRUM(Key.key("block.note_block.snare")),
        CLICK(Key.key("block.note_block.hat")),
        GUITAR(Key.key("block.note_block.guitar")),
        FLUTE(Key.key("block.note_block.flute")),
        BELL(Key.key("block.note_block.bell")),
        CHIME(Key.key("block.note_block.chime")),
        XYLOPHONE(Key.key("block.note_block.xylophone")),
        IRON_XYLOPHONE(Key.key("block.note_block.iron_xylophone")),
        COW_BELL(Key.key("block.note_block.cow_bell")),
        DIGERIDOO(Key.key("block.note_block.didgeridoo")),
        BIT(Key.key("block.note_block.bit")),
        BANJO(Key.key("block.note_block.banjo")),
        PLING(Key.key("block.note_block.pling"));

        private final Key key;

        DefaultInstruments(Key key) {
            this.key = key;
        }

        public @NotNull Key key() {
            return key;
        }
    }
}
