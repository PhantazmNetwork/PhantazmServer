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

public class FilesystemMapLoader implements MapLoader {
    private record FolderPaths(Path rooms, Path doors, Path shops, Path windows, Path rounds) {
        FolderPaths(Path root, MapInfo info) {
            this(root.resolve(info.roomsPath()), root.resolve(info.doorsPath()), root.resolve(info.shopsPath()), root
                    .resolve(info.windowsPath()), root.resolve(info.roundsPath()));
        }
    }

    private static final String MAP_DATA_EXTENSION = ".mapconfig";
    private static final String MAP_INFO_NAME = "info" + MAP_DATA_EXTENSION;
    private static final BiPredicate<Path, BasicFileAttributes> CONFIG_PREDICATE = (path, attr) -> attr.isRegularFile()
            && path.getFileName().toString().endsWith(MAP_DATA_EXTENSION);

    private final Path root;
    private final ConfigCodec codec;

    public FilesystemMapLoader(@NotNull Path root, @NotNull ConfigCodec codec) {
        this.root = Objects.requireNonNull(root, "root");
        this.codec = Objects.requireNonNull(codec, "codec");
    }

    @Override
    public @NotNull ZombiesMap load(@NotNull String mapName) throws IOException {
        Path mapDirectory = FileUtils.findFirstOrThrow(root, (path, attr) -> attr.isDirectory() && path
                .endsWith(mapName), () -> "Unable to find map folder for " + mapName);

        Path mapInfoFile = mapDirectory.resolve(MAP_INFO_NAME);

        MapInfo mapInfo = ConfigBridges.read(mapInfoFile, codec, MapProcessors.mapInfo());
        FolderPaths paths = new FolderPaths(mapDirectory, mapInfo);
        List<RoomInfo> rooms = new ArrayList<>();
        List<DoorInfo> doors = new ArrayList<>();
        List<ShopInfo> shops = new ArrayList<>();
        List<WindowInfo> windows = new ArrayList<>();
        List<RoundInfo> rounds = new ArrayList<>();

        FileUtils.forEachFileMatching(paths.rooms, CONFIG_PREDICATE, file -> rooms.add(ConfigBridges.read(file, codec,
                MapProcessors.roomInfo())));

        FileUtils.forEachFileMatching(paths.doors, CONFIG_PREDICATE, file -> doors.add(ConfigBridges.read(file, codec,
                MapProcessors.doorInfo())));

        FileUtils.forEachFileMatching(paths.shops, CONFIG_PREDICATE, file -> shops.add(ConfigBridges.read(file, codec,
                MapProcessors.shopInfo())));

        FileUtils.forEachFileMatching(paths.windows, CONFIG_PREDICATE, file -> windows.add(ConfigBridges.read(file,
                codec, MapProcessors.windowInfo())));

        FileUtils.forEachFileMatching(paths.rounds, CONFIG_PREDICATE, file -> rounds.add(ConfigBridges.read(file, codec,
                MapProcessors.roundInfo())));

        rounds.sort(Comparator.comparingInt(RoundInfo::round));

        return new ZombiesMap(mapInfo, rooms, doors, shops, windows, rounds);
    }

    @Override
    public void save(@NotNull ZombiesMap data) throws IOException {
        Path mapDirectory = root.resolve(data.info().id().value());
        Files.createDirectories(mapDirectory);

        MapInfo mapInfo = data.info();
        ConfigBridges.write(mapDirectory.resolve(MAP_INFO_NAME), MapProcessors.mapInfo().elementFromData(mapInfo),
                codec);

        FolderPaths paths = new FolderPaths(mapDirectory, mapInfo);

        FileUtils.deleteRecursivelyIfExists(paths.rooms);
        FileUtils.deleteRecursivelyIfExists(paths.doors);
        FileUtils.deleteRecursivelyIfExists(paths.shops);
        FileUtils.deleteRecursivelyIfExists(paths.windows);
        FileUtils.deleteRecursivelyIfExists(paths.rounds);

        Files.createDirectories(paths.rooms);
        Files.createDirectories(paths.doors);
        Files.createDirectories(paths.shops);
        Files.createDirectories(paths.windows);
        Files.createDirectories(paths.rounds);

        for(RoomInfo room : data.rooms()) {
            ConfigBridges.write(paths.rooms.resolve(room.id().value() + MAP_DATA_EXTENSION), MapProcessors
                            .roomInfo().elementFromData(room), codec);
        }

        int i = 0;
        for(DoorInfo door : data.doors()) {
            ConfigBridges.write(paths.doors.resolve("door_" + i + MAP_DATA_EXTENSION), MapProcessors.doorInfo()
                    .elementFromData(door), codec);
            i++;
        }

        for(ShopInfo shop : data.shops()) {
            ConfigBridges.write(paths.shops.resolve(shop.id().value()), MapProcessors.shopInfo().elementFromData(shop),
                    codec);
        }

        int j = 0;
        for(WindowInfo window : data.windows()) {
            ConfigBridges.write(paths.windows.resolve(window.room().value() + "_window_" + j +
                    MAP_DATA_EXTENSION), MapProcessors.windowInfo().elementFromData(window), codec);
            j++;
        }

        for(RoundInfo round : data.rounds()) {
            ConfigBridges.write(paths.rounds.resolve("round_" + round.round() + MAP_DATA_EXTENSION),
                    MapProcessors.roundInfo().elementFromData(round), codec);
        }
    }
}
