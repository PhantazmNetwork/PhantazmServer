package org.phantazm.server;

import com.github.steanky.ethylene.codec.json.JsonCodec;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.sound.NBSSongLoader;
import org.phantazm.core.sound.SongLoader;

import java.nio.file.Path;

public class SongFeature {
    private static final Path SONG_PATH = Path.of("./songs");

    private static SongLoader songLoader;

    static void initialize() {
        SongFeature.songLoader = new NBSSongLoader(SONG_PATH, new JsonCodec());
        SongFeature.songLoader.load();
    }

    public static @NotNull SongLoader songLoader() {
        return FeatureUtils.check(songLoader);
    }
}
