package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.commons.FileUtils;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.steanky.ethylene.core.bridge.ConfigBridges;
import com.github.steanky.ethylene.core.codec.ConfigCodec;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

public class FileSystemMapLoader implements MapLoader {
    private static final String ROOMS_PATH = "rooms";
    private static final String DOORS_PATH = "doors";
    private static final String SHOPS_PATH = "shops";
    private static final String WINDOWS_PATH = "windows";
    private static final String ROUNDS_PATH = "rounds";
    private static final String SPAWNRULES_PATH = "spawnrules";
    private static final String SPAWNPOINTS_PATH = "spawnpoints";

    private record FolderPaths(Path rooms, Path doors, Path shops, Path windows, Path rounds, Path spawnrules,
                               Path spawnpoints) {
        FolderPaths(Path root) {
            this(root.resolve(ROOMS_PATH), root.resolve(DOORS_PATH), root.resolve(SHOPS_PATH), root
                    .resolve(WINDOWS_PATH), root.resolve(ROUNDS_PATH), root.resolve(SPAWNRULES_PATH), root
                    .resolve(SPAWNPOINTS_PATH));
        }
    }
    private final String mapInfoName;
    private final BiPredicate<Path, BasicFileAttributes> configPredicate;

    private final Path root;
    private final ConfigCodec codec;

    public FileSystemMapLoader(@NotNull Path root, @NotNull ConfigCodec codec) {
        this.root = Objects.requireNonNull(root, "root");
        this.codec = Objects.requireNonNull(codec, "codec");

        String preferredExtension = codec.getPreferredExtension();
        this.configPredicate = (path, attr) -> attr.isRegularFile() && path.getFileName().toString()
                .endsWith(preferredExtension);
        this.mapInfoName = "info." + preferredExtension;
    }

    @Override
    public @NotNull MapInfo load(@NotNull String mapName) throws IOException {
        Path mapDirectory = FileUtils.findFirstOrThrow(root, (path, attr) -> attr.isDirectory() && path
                .endsWith(mapName), () -> "Unable to find map folder for " + mapName);

        Path mapInfoFile = mapDirectory.resolve(mapInfoName);

        MapSettingsInfo mapSettingsInfo = ConfigBridges.read(mapInfoFile, codec, MapProcessors.mapInfo());
        FolderPaths paths = new FolderPaths(mapDirectory);
        List<RoomInfo> rooms = new ArrayList<>();
        List<DoorInfo> doors = new ArrayList<>();
        List<ShopInfo> shops = new ArrayList<>();
        List<WindowInfo> windows = new ArrayList<>();
        List<RoundInfo> rounds = new ArrayList<>();
        List<SpawnruleInfo> spawnrules = new ArrayList<>();
        List<SpawnpointInfo> spawnpoints = new ArrayList<>();

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

        FileUtils.forEachFileMatching(paths.spawnrules, configPredicate, file -> spawnrules.add(ConfigBridges.read(file,
                codec, MapProcessors.spawnruleInfo())));

        FileUtils.forEachFileMatching(paths.spawnpoints, configPredicate, file -> spawnpoints.add(ConfigBridges
                .read(file, codec, MapProcessors.spawnpointInfo())));

        rounds.sort(Comparator.comparingInt(RoundInfo::round));

        return new MapInfo(mapSettingsInfo, rooms, doors, shops, windows, rounds, spawnrules, spawnpoints);
    }

    @Override
    public void save(@NotNull MapInfo data) throws IOException {
        Path mapDirectory = root.resolve(data.info().id().value());
        Files.createDirectories(mapDirectory);

        MapSettingsInfo mapSettingsInfo = data.info();
        ConfigBridges.write(mapDirectory.resolve(mapInfoName), MapProcessors.mapInfo().elementFromData(mapSettingsInfo), codec);

        FolderPaths paths = new FolderPaths(mapDirectory);

        FileUtils.deleteRecursivelyIfExists(paths.rooms);
        FileUtils.deleteRecursivelyIfExists(paths.doors);
        FileUtils.deleteRecursivelyIfExists(paths.shops);
        FileUtils.deleteRecursivelyIfExists(paths.windows);
        FileUtils.deleteRecursivelyIfExists(paths.rounds);
        FileUtils.deleteRecursivelyIfExists(paths.spawnrules);
        FileUtils.deleteRecursivelyIfExists(paths.spawnpoints);

        Files.createDirectories(paths.rooms);
        Files.createDirectories(paths.doors);
        Files.createDirectories(paths.shops);
        Files.createDirectories(paths.windows);
        Files.createDirectories(paths.rounds);
        Files.createDirectories(paths.spawnrules);
        Files.createDirectories(paths.spawnpoints);

        String extension = "." + codec.getPreferredExtension();

        for(RoomInfo room : data.rooms()) {
            ConfigBridges.write(paths.rooms.resolve(room.id().value() + extension), codec, MapProcessors
                    .roomInfo(), room);
        }

        for(DoorInfo door : data.doors()) {
            ConfigBridges.write(paths.doors.resolve(door.id().value() + extension), MapProcessors
                    .doorInfo().elementFromData(door), codec);
        }

        for(ShopInfo shop : data.shops()) {
            ConfigBridges.write(paths.shops.resolve(getPositionString(shop.triggerLocation()) + "-" + shop.id()
                    .value() + extension), MapProcessors.shopInfo().elementFromData(shop), codec);
        }

        int i = 0;
        for(WindowInfo window : data.windows()) {
            Vec3I origin = window.frameRegion().getOrigin();
            ConfigBridges.write(paths.windows.resolve(getPositionString(origin) + "-" + i + extension),
                    MapProcessors.windowInfo().elementFromData(window), codec);
            i++;
        }

        for(RoundInfo round : data.rounds()) {
            ConfigBridges.write(paths.rounds.resolve(round.round() + extension), MapProcessors.roundInfo()
                    .elementFromData(round), codec);
        }

        for(SpawnruleInfo spawnrule : data.spawnrules()) {
            ConfigBridges.write(paths.spawnrules.resolve(spawnrule.id().value() + extension), MapProcessors
                    .spawnruleInfo().elementFromData(spawnrule), codec);
        }

        int j = 0;
        for(SpawnpointInfo spawnpoint : data.spawnpoints()) {
            ConfigBridges.write(paths.spawnpoints.resolve(getPositionString(spawnpoint.position()) + "-" + j +
                    extension), MapProcessors.spawnpointInfo().elementFromData(spawnpoint), codec);
            j++;
        }
    }

    private String getPositionString(Vec3I position) {
        return position.getX() + "_" + position.getY() + "_" + position.getZ();
    }
}
