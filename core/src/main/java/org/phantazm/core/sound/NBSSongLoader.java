package org.phantazm.core.sound;

import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Stream;

public class NBSSongLoader implements SongLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(NBSSongLoader.class);

    private static final int MIN_KEY = 33;
    private static final int MAX_KEY = 57;

    private static final DefaultInstruments[] DEFAULT_INSTRUMENT_VALUES = DefaultInstruments.values();

    private final Path rootPath;
    private final KeyParser keyParser;

    private Map<Key, List<SongPlayer.Note>> map;

    public NBSSongLoader(@NotNull Path rootPath, @NotNull KeyParser keyParser) {
        this.rootPath = Objects.requireNonNull(rootPath, "rootPath");
        this.keyParser = Objects.requireNonNull(keyParser, "keyParser");
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

        try (Stream<Path> pathStream = Files.list(rootPath)) {
            pathStream.forEach(path -> {
                String fileName = path.getFileName().toString();

                if (fileName.endsWith(".nbs")) {
                    OptionalInt versionOptional = hintNBSVersion(path);

                    Optional<List<SongPlayer.Note>> notesOptional;
                    if (versionOptional.isPresent() &&
                            (notesOptional = loadVersioned(versionOptional.getAsInt(), path)).isPresent()) {
                        List<SongPlayer.Note> notes = notesOptional.get();
                        //safe to pass result of lastIndexOf since we know fileName contains a '.'
                        @Subst(Constants.NAMESPACE_OR_KEY)
                        String extensionless = fileName.substring(0, fileName.lastIndexOf('.'));

                        if (keyParser.isValidKey(extensionless)) {
                            Key key = keyParser.parseKey(extensionless);
                            if (map.putIfAbsent(key, notes) != null) {
                                LOGGER.warn("Duplicate song key {}", key);
                            }
                        }
                    }
                    else {
                        LOGGER.warn("Failed to load song from {}", path);
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

    private static Optional<List<SongPlayer.Note>> loadVersioned(int versionHint, Path path) {
        return switch (versionHint) {
            case 5, 4 -> OpenNBSv5.load(path);
            default -> {
                LOGGER.warn("{} uses an unsupported version of the NBS protocol", path);
                yield Optional.empty();
            }
        };
    }

    /**
     * Tries to determine the version used by a NBS file at the given path. Returns an empty Optional if there is some
     * error.
     *
     * @param path the file to inspect
     * @return an optional containing the NBS version, which will be empty if there is an error
     */
    private static OptionalInt hintNBSVersion(Path path) {
        try (InputStream stream = Files.newInputStream(path, StandardOpenOption.READ)) {
            short protocol = readShort(stream);
            if (protocol != 0) {
                return OptionalInt.of(0); //indicates an old NBS Classic file
            }

            return OptionalInt.of(readByte(stream));
        }
        catch (IOException e) {
            LOGGER.warn("Error inferring NBS file version from {}: {}", path, e);
        }

        return OptionalInt.empty();
    }

    /**
     * Utilities for reading from .nbs files encoded according to the OpenNBS 5 spec. Backwards compatible only with
     * version 4.
     */
    private static class OpenNBSv5 {

        //intermediate data structure (instruments are known only after all notes are loaded, in meantime we store index)
        private record NBSNote(int delayTick, int instrumentIndex, float normalizedPitch) {
        }

        /**
         * Loads a file according to the OpenNBS spec 5.0 (reference <a href="https://opennbs.org/nbs">here</a>). Logs an
         * error and returns an empty Optional if there is some problem. This will correctly read
         * files encoded in Version 4 as well.
         *
         * @param path the file to load from
         * @return an empty {@link Optional} on error, otherwise a list of notes loaded from the NBS file
         */
        @SuppressWarnings("unused") //unused variables are useful to indicate type & meaning of parts of the spec
        public static @NotNull Optional<List<SongPlayer.Note>> load(@NotNull Path path) {
            try (InputStream stream = new BufferedInputStream(Files.newInputStream(path, StandardOpenOption.READ))) {
                short protocol = readShort(stream);
                if (protocol != 0) {
                    LOGGER.warn("Invalid NBS file {} (was it created using an old version of NBS?)", path);
                    return Optional.empty();
                }

                byte nbsVersion = readByte(stream);
                if (nbsVersion != 5) {
                    LOGGER.warn("Invalid NBS file {} reported unsupported protocol version {}", path, nbsVersion);
                    return Optional.empty();
                }

                //read NBS header
                byte vanillaInstrumentCount = readByte(stream);
                short songLength = readShort(stream);
                short layerCount = readShort(stream);
                String songName = readPrefixedString(stream);
                String songAuthor = readPrefixedString(stream);
                String songOriginalAuthor = readPrefixedString(stream);
                String songDescription = readPrefixedString(stream);
                short songTempo = readShort(stream);
                byte autoSaving = readByte(stream);
                byte autoSavingDuration = readByte(stream);
                byte timeSignature = readByte(stream);
                int minutesSpent = readInt(stream);
                int leftClicks = readInt(stream);
                int rightClicks = readInt(stream);
                int noteBlocksAdded = readInt(stream);
                int noteBlocksRemoved = readInt(stream);
                String midiSchematicFileName = readPrefixedString(stream);
                byte loop = readByte(stream);
                byte maxLoopCount = readByte(stream);
                short loopStartTick = readByte(stream);

                List<NBSNote> notes = new ArrayList<>();

                short actualTick = Short.MAX_VALUE;
                short lastActualTick = 0;

                while (true) {
                    short jumpsToNextTick = readShort(stream);
                    if (jumpsToNextTick == 0) {
                        break;
                    }

                    actualTick += jumpsToNextTick;

                    short actualLayer = Short.MAX_VALUE;
                    boolean first = true;

                    while (true) {
                        short jumpsToNextLayer = readShort(stream);
                        if (jumpsToNextLayer == 0) {
                            break;
                        }

                        actualLayer += jumpsToNextLayer;

                        short delayTick = first ? normalizeTick(songTempo, (short)(actualTick - lastActualTick)) : 0;
                        byte noteBlockInstrument = readByte(stream);
                        if (noteBlockInstrument < 0) {
                            LOGGER.warn("Negative note index in {}", path);
                            return Optional.empty();
                        }

                        byte noteBlockKey = readByte(stream);
                        byte noteBlockVelocity = readByte(stream);
                        int noteBlockPanning = readUnsignedByte(stream);
                        short noteBlockPitch = readShort(stream);

                        notes.add(new NBSNote(delayTick, noteBlockInstrument, normalizeKey(noteBlockKey)));
                        first = false;
                    }

                    lastActualTick = actualTick;
                }

                for (int i = 0; i < layerCount; i++) {
                    String layerName = readPrefixedString(stream);
                    byte layerLock = readByte(stream);
                    byte layerVolume = readByte(stream);
                    int layerStereo = readUnsignedByte(stream);
                }

                int customInstruments = readUnsignedByte(stream);
                Key[] customInstrumentKeys = new Key[customInstruments];
                for (int i = 0; i < customInstruments; i++) {
                    String instrumentName = readPrefixedString(stream);

                    @Subst(Constants.NAMESPACE_OR_KEY)
                    String soundFile = readPrefixedString(stream);

                    byte soundKey = readByte(stream);
                    byte pressPianoKey = readByte(stream);

                    if (!Key.parseable(soundFile)) {
                        LOGGER.warn("Bad custom instrument {} in {}, must be a valid Minecraft resource key", soundFile,
                                path);
                        return Optional.empty();
                    }
                    else {
                        customInstrumentKeys[i] = Key.key(soundFile);
                    }
                }

                List<SongPlayer.Note> actualNotes = new ArrayList<>(notes.size());
                for (NBSNote nbsNote : notes) {
                    Key instrumentKey;
                    if (nbsNote.instrumentIndex < DEFAULT_INSTRUMENT_VALUES.length) {
                        instrumentKey = DEFAULT_INSTRUMENT_VALUES[nbsNote.instrumentIndex].key;
                    }
                    else {
                        int index = nbsNote.instrumentIndex - DEFAULT_INSTRUMENT_VALUES.length;
                        if (index >= customInstrumentKeys.length) {
                            LOGGER.warn("Custom instrument index out of bounds in {}", path);
                            return Optional.empty();
                        }

                        instrumentKey = customInstrumentKeys[index];
                    }

                    Sound sound = Sound.sound(instrumentKey, Sound.Source.MUSIC, 10, nbsNote.normalizedPitch);
                    actualNotes.add(new SongPlayer.Note(sound, nbsNote.delayTick));
                }

                return Optional.of(actualNotes);
            }
            catch (IOException e) {
                LOGGER.warn("IOException loading song in {}", path);
            }

            return Optional.empty();
        }
    }

    private static float normalizeKey(int key) {
        int normalized = MathUtils.clamp(key, MIN_KEY, MAX_KEY);
        int uses = normalized - MIN_KEY;

        double pow = Math.pow(2, (uses - 12) / 12D);
        return (float)MathUtils.clamp(pow, 0, 2);
    }

    private static short normalizeTick(short tempo, short tick) {
        return (short)Math.round((double)tick * (20.0 / (double)tempo / 100.0));
    }

    private static String readPrefixedString(InputStream stream) throws IOException {
        int length = readInt(stream);
        if (length < 0) {
            throw new IOException("Invalid data");
        }

        return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(readExact(stream, length))).toString();
    }

    private static int readUnsignedByte(InputStream stream) throws IOException {
        return readByte(stream) & 0xFF;
    }

    private static byte readByte(InputStream stream) throws IOException {
        int result = stream.read();
        if (result == -1) {
            throw new EOFException();
        }

        return (byte)result;
    }

    private static int readInt(InputStream stream) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.wrap(readExact(stream, 4));
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return byteBuffer.getInt();
    }

    private static short readShort(InputStream stream) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.wrap(readExact(stream, 2));
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return byteBuffer.getShort();
    }

    private static byte[] readExact(InputStream stream, int num) throws IOException {
        byte[] bytes = stream.readNBytes(num);
        if (bytes.length != num) {
            throw new EOFException();
        }

        return bytes;
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
    }
}
