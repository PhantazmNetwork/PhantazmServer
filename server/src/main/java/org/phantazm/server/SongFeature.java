package org.phantazm.server;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.sound.NBSSongLoader;
import org.phantazm.core.sound.SongLoader;
import org.phantazm.server.context.EthyleneContext;

import java.nio.file.Path;

public class SongFeature {
    private static final Path SONG_PATH = Path.of("./songs");

    private static SongLoader songLoader;

    private SongFeature() {
    }

    static void initialize(@NotNull EthyleneContext ethyleneContext) {
        SongFeature.songLoader = new NBSSongLoader(SONG_PATH, ethyleneContext.keyParser());
        SongFeature.songLoader.load();
    }

    public static @NotNull SongLoader songLoader() {
        return FeatureUtils.check(songLoader);
    }
}
