package org.phantazm.server;

import com.github.steanky.element.core.key.KeyParser;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.sound.NBSSongLoader;
import org.phantazm.core.sound.SongLoader;

import java.nio.file.Path;

public class SongFeature {
    private static final Path SONG_PATH = Path.of("./songs");

    private static SongLoader songLoader;

    private SongFeature() {
    }

    static void initialize(@NotNull KeyParser keyParser) {
        SongFeature.songLoader = new NBSSongLoader(SONG_PATH, keyParser);
        SongFeature.songLoader.load();
    }

    public static @NotNull SongLoader songLoader() {
        return FeatureUtils.check(songLoader);
    }
}
