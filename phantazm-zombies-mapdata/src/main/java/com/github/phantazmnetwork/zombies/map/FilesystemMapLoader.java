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
    private static final String MAP_DATA_EXTENSION = ".mapconfig";
    private static final String MAP_INFO_NAME = "info" + MAP_DATA_EXTENSION;
    private static final BiPredicate<Path, BasicFileAttributes> CONFIG_PREDICATE = (path, attributes) -> attributes
            .isRegularFile() && path.toString().endsWith(MAP_DATA_EXTENSION);

    private final Path root;
    private final ConfigCodec codec;

    public FilesystemMapLoader(@NotNull Path root, @NotNull ConfigCodec codec) {
        this.root = Objects.requireNonNull(root, "root");
        this.codec = Objects.requireNonNull(codec, "codec");
    }

    @Override
    public @NotNull ZombiesMap load(@NotNull String mapName) throws IOException {
        Path mapFolder = FileUtils.findFirstOrThrow(root, (path, basicFileAttributes) -> basicFileAttributes
                .isDirectory() && path.endsWith(mapName), () -> "Unable to find map folder for " + mapName);

        Path mapInfoFile = mapFolder.resolve(MAP_INFO_NAME);

        MapInfo mapInfo = ConfigBridges.read(mapInfoFile, codec, MapProcessors.mapInfo());
        Path roomsFolder = root.resolve(mapInfo.roomsPath());
        Path doorsFolder = root.resolve(mapInfo.doorsPath());
        Path shopsFolder = root.resolve(mapInfo.shopsPath());
        Path windowsFolder = root.resolve(mapInfo.windowsPath());
        Path roundsFolder = root.resolve(mapInfo.roundsPath());

        List<RoomInfo> rooms = new ArrayList<>();
        List<DoorInfo> doors = new ArrayList<>();
        List<ShopInfo> shops = new ArrayList<>();
        List<WindowInfo> windows = new ArrayList<>();
        List<RoundInfo> rounds = new ArrayList<>();

        FileUtils.forEachFileMatching(roomsFolder, CONFIG_PREDICATE, file -> rooms.add(ConfigBridges.read(file, codec,
                MapProcessors.roomInfo())));

        FileUtils.forEachFileMatching(doorsFolder, CONFIG_PREDICATE, file -> doors.add(ConfigBridges.read(file, codec,
                MapProcessors.doorInfo())));

        FileUtils.forEachFileMatching(shopsFolder, CONFIG_PREDICATE, file -> shops.add(ConfigBridges.read(file, codec,
                MapProcessors.shopInfo())));

        FileUtils.forEachFileMatching(windowsFolder, CONFIG_PREDICATE, file -> windows.add(ConfigBridges.read(file,
                codec, MapProcessors.windowInfo())));

        FileUtils.forEachFileMatching(roundsFolder, CONFIG_PREDICATE, file -> rounds.add(ConfigBridges.read(file, codec,
                MapProcessors.roundInfo())));

        rounds.sort(Comparator.comparingInt(RoundInfo::round));

        return new ZombiesMap(mapInfo, Collections.unmodifiableList(rooms), Collections.unmodifiableList(doors),
                Collections.unmodifiableList(shops), Collections.unmodifiableList(windows), Collections
                .unmodifiableList(rounds));
    }

    @Override
    public void save(@NotNull ZombiesMap data) throws IOException {
        Path mapDirectory = root.resolve(data.info().id().value());
        Files.createDirectories(mapDirectory);

        MapInfo mapInfo = data.info();
        Path roomsFolder = root.resolve(mapInfo.roomsPath());
        Path doorsFolder = root.resolve(mapInfo.doorsPath());
        Path shopsFolder = root.resolve(mapInfo.shopsPath());
        Path windowsFolder = root.resolve(mapInfo.windowsPath());
        Path roundsFolder = root.resolve(mapInfo.roundsPath());

        FileUtils.deleteRecursivelyIfExists(roomsFolder);
        FileUtils.deleteRecursivelyIfExists(doorsFolder);
        FileUtils.deleteRecursivelyIfExists(shopsFolder);
        FileUtils.deleteRecursivelyIfExists(windowsFolder);
        FileUtils.deleteRecursivelyIfExists(roundsFolder);

        Files.createDirectories(roomsFolder);
        Files.createDirectories(doorsFolder);
        Files.createDirectories(shopsFolder);
        Files.createDirectories(windowsFolder);
        Files.createDirectories(roundsFolder);

        for(RoomInfo room : data.rooms()) {
            ConfigBridges.write(roomsFolder.resolve(room.id().value() + MAP_DATA_EXTENSION), MapProcessors
                            .roomInfo().elementFromData(room), codec);
        }

        int i = 0;
        for(DoorInfo door : data.doors()) {
            ConfigBridges.write(doorsFolder.resolve("door_" + i + MAP_DATA_EXTENSION), MapProcessors.doorInfo()
                    .elementFromData(door), codec);
            i++;
        }

        for(ShopInfo shop : data.shops()) {
            ConfigBridges.write(shopsFolder.resolve(shop.id().value()), MapProcessors.shopInfo().elementFromData(shop),
                    codec);
        }

        int j = 0;
        for(WindowInfo window : data.windows()) {
            ConfigBridges.write(windowsFolder.resolve(window.room().value() + "_window_" + j), MapProcessors
                            .windowInfo().elementFromData(window), codec);
            j++;
        }

        for(RoundInfo round : data.rounds()) {
            ConfigBridges.write(roundsFolder.resolve("round_" + round.round()), MapProcessors.roundInfo()
                    .elementFromData(round), codec);
        }
    }
}
