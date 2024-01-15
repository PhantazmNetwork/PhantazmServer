package org.phantazm.zombies.map;

import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;
import org.phantazm.loader.Loader;
import org.phantazm.loader.ObjectExtractor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.phantazm.loader.DataSource.*;

public final class MapLoader {
    public static @NotNull Loader<MapInfo> mapInfoLoader(@NotNull Path mapPath, @NotNull ConfigCodec codec,
        @NotNull ConfigProcessor<MapInfo> mapInfoProcessor) {
        return Loader.loader(() -> {
            return composite(path -> {
                return merged(namedList(path, codec, "doors"),
                    namedList(path, codec, "rooms"),
                    namedList(path, codec, "rounds"),
                    namedList(path, codec, "shops"),
                    namedList(path, codec, "spawnpoints"),
                    namedList(path, codec, "spawnrules"),
                    namedList(path, codec, "windows"),
                    namedSingle(path, codec, "coins.yml", "playerCoins"),
                    namedSingle(path, codec, "corpse.yml", "corpse"),
                    namedSingle(path, codec, "endless.yml", "endless"),
                    namedSingle(path, codec, "leaderboard.yml", "leaderboard"),
                    namedSingle(path, codec, "settings.yml", "settings"),
                    namedSingle(path, codec, "sidebar.yml", "scoreboard"),
                    namedSingle(path, codec, "webhook.yml", "webhook"));
            }, Files.list(mapPath));
        }, ObjectExtractor.extractor(ConfigNode.class, (dataLocation, element) -> {
            MapInfo info = mapInfoProcessor.dataFromElement(element);
            return List.of(ObjectExtractor.entry(info.settings().id(), info));
        }));
    }
}
