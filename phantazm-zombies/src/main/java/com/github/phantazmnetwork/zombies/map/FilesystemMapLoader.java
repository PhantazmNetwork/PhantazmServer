package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.commons.FileUtils;
import com.github.steanky.ethylene.core.bridge.ConfigBridges;
import com.github.steanky.ethylene.core.codec.ConfigCodec;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

public class FilesystemMapLoader implements MapLoader {
    private static final String MAP_DATA_EXTENSION = ".mapconfig";
    private static final String MAP_INFO_NAME = "map" + MAP_DATA_EXTENSION;
    private static final BiPredicate<Path, BasicFileAttributes> CONFIG_PREDICATE = (path, attributes) -> attributes
            .isRegularFile() && path.toString().endsWith(MAP_DATA_EXTENSION);

    private final Path root;
    private final ConfigCodec codec;

    public FilesystemMapLoader(@NotNull Path root, @NotNull ConfigCodec codec) {
        this.root = Objects.requireNonNull(root);
        this.codec = codec;
    }

    @Override
    public @NotNull MapData load(@NotNull String mapName) throws IOException {
        Path mapFolder = FileUtils.findFirstOrThrow(root, (path, basicFileAttributes) -> basicFileAttributes
                .isDirectory() && path.endsWith(mapName), () -> "Unable to find map folder for " + mapName);

        Path mapInfoFile = mapFolder.resolve(MAP_INFO_NAME);

        MapInfo mapInfo = ConfigBridges.read(mapInfoFile, codec, MapProcessors.mapInfo());
        Path roomsFolder = root.resolve(mapInfo.roomsPath());
        Path windowsFolder = root.resolve(mapInfo.windowsPath());
        Path roundsFolder = root.resolve(mapInfo.roundsPath());

        List<RoomInfo> rooms = new ArrayList<>();
        List<DoorInfo> doors = new ArrayList<>();
        List<WindowInfo> windows = new ArrayList<>();
        List<RoundInfo> rounds = new ArrayList<>();

        FileUtils.forEachFileMatching(roomsFolder, CONFIG_PREDICATE, file -> rooms.add(ConfigBridges.read(file, codec,
                MapProcessors.roomInfo())));

        FileUtils.forEachFileMatching(roomsFolder, CONFIG_PREDICATE, file -> doors.add(ConfigBridges.read(file, codec,
                MapProcessors.doorInfo())));

        FileUtils.forEachFileMatching(windowsFolder, CONFIG_PREDICATE, file -> windows.add(ConfigBridges.read(file,
                codec, MapProcessors.windowInfo())));

        FileUtils.forEachFileMatching(roundsFolder, CONFIG_PREDICATE, file -> rounds.add(ConfigBridges.read(file,
                codec, MapProcessors.roundInfo())));

        rounds.sort(Comparator.comparingInt(RoundInfo::round));

        return new MapData(mapInfo, Collections.unmodifiableList(rooms), Collections.unmodifiableList(doors),
                Collections.unmodifiableList(windows), Collections.unmodifiableList(rounds));
    }

    @Override
    public void save(@NotNull MapData data, @NotNull String mapName) {

    }
}
