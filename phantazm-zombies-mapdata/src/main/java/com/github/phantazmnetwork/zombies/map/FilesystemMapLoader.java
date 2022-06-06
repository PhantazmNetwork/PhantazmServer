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
    private static final String ROOMS_PATH = "rooms";
    private static final String DOORS_PATH = "doors";
    private static final String SHOPS_PATH = "shops";
    private static final String WINDOWS_PATH = "windows";
    private static final String ROUNDS_PATH = "rounds";

    private record FolderPaths(Path rooms, Path doors, Path shops, Path windows, Path rounds) {
        FolderPaths(Path root) {
            this(root.resolve(ROOMS_PATH), root.resolve(DOORS_PATH), root.resolve(SHOPS_PATH), root
                    .resolve(WINDOWS_PATH), root.resolve(ROUNDS_PATH));
        }
    }
    private final String mapInfoName;
    private final BiPredicate<Path, BasicFileAttributes> configPredicate;

    private final Path root;
    private final ConfigCodec codec;

    public FilesystemMapLoader(@NotNull Path root, @NotNull ConfigCodec codec) {
        this.root = Objects.requireNonNull(root, "root");
        this.codec = Objects.requireNonNull(codec, "codec");

        String preferredExtension = codec.getPreferredExtension();
        this.configPredicate = (path, attr) -> attr.isRegularFile() && path.getFileName().toString()
                .endsWith(preferredExtension);
        this.mapInfoName = "info." + preferredExtension;
    }

    @Override
    public @NotNull ZombiesMap load(@NotNull String mapName) throws IOException {
        Path mapDirectory = FileUtils.findFirstOrThrow(root, (path, attr) -> attr.isDirectory() && path
                .endsWith(mapName), () -> "Unable to find map folder for " + mapName);

        Path mapInfoFile = mapDirectory.resolve(mapInfoName);

        MapInfo mapInfo = ConfigBridges.read(mapInfoFile, codec, MapProcessors.mapInfo());
        FolderPaths paths = new FolderPaths(mapDirectory);
        List<RoomInfo> rooms = new ArrayList<>();
        List<DoorInfo> doors = new ArrayList<>();
        List<ShopInfo> shops = new ArrayList<>();
        List<WindowInfo> windows = new ArrayList<>();
        List<RoundInfo> rounds = new ArrayList<>();

        FileUtils.forEachFileMatching(paths.rooms, configPredicate, file -> rooms.add(ConfigBridges.read(file, codec,
                MapProcessors.roomInfo())));

        FileUtils.forEachFileMatching(paths.doors, configPredicate, file -> doors.add(ConfigBridges.read(file, codec,
                MapProcessors.doorInfo())));

        FileUtils.forEachFileMatching(paths.shops, configPredicate, file -> shops.add(ConfigBridges.read(file, codec,
                MapProcessors.shopInfo())));

        FileUtils.forEachFileMatching(paths.windows, configPredicate, file -> windows.add(ConfigBridges.read(file,
                codec, MapProcessors.windowInfo())));

        FileUtils.forEachFileMatching(paths.rounds, configPredicate, file -> rounds.add(ConfigBridges.read(file, codec,
                MapProcessors.roundInfo())));

        rounds.sort(Comparator.comparingInt(RoundInfo::round));

        return new ZombiesMap(mapInfo, rooms, doors, shops, windows, rounds);
    }

    @Override
    public void save(@NotNull ZombiesMap data) throws IOException {
        Path mapDirectory = root.resolve(data.info().id().value());
        Files.createDirectories(mapDirectory);

        MapInfo mapInfo = data.info();
        ConfigBridges.write(mapDirectory.resolve(mapInfoName), MapProcessors.mapInfo().elementFromData(mapInfo), codec);

        FolderPaths paths = new FolderPaths(mapDirectory);

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
            ConfigBridges.write(paths.rooms.resolve(room.id().value() + "." + codec.getPreferredExtension()),
                    codec, MapProcessors.roomInfo(), room);
        }

        int i = 0;
        for(DoorInfo door : data.doors()) {
            ConfigBridges.write(paths.doors.resolve("door_" + i + "." + codec.getPreferredExtension()),
                    MapProcessors.doorInfo().elementFromData(door), codec);
            i++;
        }

        for(ShopInfo shop : data.shops()) {
            ConfigBridges.write(paths.shops.resolve(shop.id().value() + "." + codec.getPreferredExtension()),
                    MapProcessors.shopInfo().elementFromData(shop), codec);
        }

        int j = 0;
        for(WindowInfo window : data.windows()) {
            ConfigBridges.write(paths.windows.resolve(window.room().value() + "_window_" + j + "." +
                    codec.getPreferredExtension()), MapProcessors.windowInfo().elementFromData(window), codec);
            j++;
        }

        for(RoundInfo round : data.rounds()) {
            ConfigBridges.write(paths.rounds.resolve("round_" + round.round() + "." + codec
                    .getPreferredExtension()), MapProcessors.roundInfo().elementFromData(round), codec);
        }
    }
}
