package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.commons.FileUtils;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.bridge.Configuration;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.BiPredicate;

/**
 * A {@link MapLoader} implementation that loads maps from a filesystem.
 */
public class FileSystemMapLoader implements MapLoader {
    private static final String ROOMS_PATH = "rooms";
    private static final String DOORS_PATH = "doors";
    private static final String SHOPS_PATH = "shops";
    private static final String WINDOWS_PATH = "windows";
    private static final String ROUNDS_PATH = "rounds";
    private static final String SPAWNRULES_PATH = "spawnrules";
    private static final String SPAWNPOINTS_PATH = "spawnpoints";
    private static final String SCOREBOARD_PATH = "scoreboard";
    private final String mapInfoName;
    private final BiPredicate<Path, BasicFileAttributes> configPredicate;
    private final Path root;
    private final ConfigCodec codec;

    /**
     * Constructs a new instance of this class from the provided root path and using the provided {@link ConfigCodec} to
     * serialize/deserialize map data files.
     *
     * @param root  the root path from which to search for map information
     * @param codec the codec used to serialize/deserialize map data files
     */
    public FileSystemMapLoader(@NotNull Path root, @NotNull ConfigCodec codec) {
        this.root = Objects.requireNonNull(root, "root");
        this.codec = Objects.requireNonNull(codec, "codec");

        Set<String> preferredExtensions = codec.getPreferredExtensions();
        if (preferredExtensions.isEmpty()) {
            this.configPredicate = (path, attrs) -> true;
            this.mapInfoName = "settings";
        }
        else {
            this.configPredicate = (path, attr) -> {
                if (!attr.isRegularFile()) {
                    return false;
                }

                for (String extension : preferredExtensions) {
                    if (path.getFileName().toString().endsWith(extension)) {
                        return true;
                    }
                }

                return false;
            };
            this.mapInfoName = "settings." + codec.getPreferredExtension();
        }
    }

    @Override
    public @NotNull MapInfo load(@NotNull String mapName) throws IOException {
        Path mapDirectory = mapDirectoryFromName(mapName);

        Path mapInfoFile = mapDirectory.resolve(mapInfoName);

        MapSettingsInfo mapSettingsInfo = Configuration.read(mapInfoFile, codec, MapProcessors.mapInfo());
        FolderPaths paths = new FolderPaths(mapDirectory);
        List<RoomInfo> rooms = new ArrayList<>();
        List<DoorInfo> doors = new ArrayList<>();
        List<ShopInfo> shops = new ArrayList<>();
        List<WindowInfo> windows = new ArrayList<>();
        List<RoundInfo> rounds = new ArrayList<>();
        List<SpawnruleInfo> spawnrules = new ArrayList<>();
        List<SpawnpointInfo> spawnpoints = new ArrayList<>();
        ConfigNode scoreboard;

        FileUtils.forEachFileMatching(paths.rooms, configPredicate,
                file -> rooms.add(Configuration.read(file, codec, MapProcessors.roomInfo())));

        FileUtils.forEachFileMatching(paths.doors, configPredicate,
                file -> doors.add(Configuration.read(file, codec, MapProcessors.doorInfo())));

        FileUtils.forEachFileMatching(paths.shops, configPredicate,
                file -> shops.add(Configuration.read(file, codec, MapProcessors.shopInfo())));

        FileUtils.forEachFileMatching(paths.windows, configPredicate,
                file -> windows.add(Configuration.read(file, codec, MapProcessors.windowInfo())));

        FileUtils.forEachFileMatching(paths.rounds, configPredicate,
                file -> rounds.add(Configuration.read(file, codec, MapProcessors.roundInfo())));
        rounds.sort(Comparator.comparingInt(RoundInfo::round));

        FileUtils.forEachFileMatching(paths.spawnrules, configPredicate,
                file -> spawnrules.add(Configuration.read(file, codec, MapProcessors.spawnruleInfo())));

        FileUtils.forEachFileMatching(paths.spawnpoints, configPredicate,
                file -> spawnpoints.add(Configuration.read(file, codec, MapProcessors.spawnpointInfo())));

        String scoreboardSettingsPath =
                "settings" + (codec.getPreferredExtensions().isEmpty() ? "" : "." + codec.getPreferredExtension());
        scoreboard =
                Configuration.read(paths.scoreboard.resolve(scoreboardSettingsPath), codec, MapProcessors.scoreboard());

        return new MapInfo(mapSettingsInfo, rooms, doors, shops, windows, rounds, spawnrules, spawnpoints, scoreboard);
    }

    @Override
    public void save(@NotNull MapInfo data) throws IOException {
        Path mapDirectory = root.resolve(data.settings().id().value());
        Files.createDirectories(mapDirectory);

        MapSettingsInfo mapSettingsInfo = data.settings();
        Configuration.write(mapDirectory.resolve(mapInfoName), MapProcessors.mapInfo().elementFromData(mapSettingsInfo),
                codec);

        FolderPaths paths = new FolderPaths(mapDirectory);

        FileUtils.deleteRecursivelyIfExists(paths.rooms);
        FileUtils.deleteRecursivelyIfExists(paths.doors);
        FileUtils.deleteRecursivelyIfExists(paths.shops);
        FileUtils.deleteRecursivelyIfExists(paths.windows);
        FileUtils.deleteRecursivelyIfExists(paths.rounds);
        FileUtils.deleteRecursivelyIfExists(paths.spawnrules);
        FileUtils.deleteRecursivelyIfExists(paths.spawnpoints);
        FileUtils.deleteRecursivelyIfExists(paths.scoreboard);

        Files.createDirectories(paths.rooms);
        Files.createDirectories(paths.doors);
        Files.createDirectories(paths.shops);
        Files.createDirectories(paths.windows);
        Files.createDirectories(paths.rounds);
        Files.createDirectories(paths.spawnrules);
        Files.createDirectories(paths.spawnpoints);
        Files.createDirectories(paths.scoreboard);

        String extension = codec.getPreferredExtensions().isEmpty() ? "" : "." + codec.getPreferredExtension();

        for (RoomInfo room : data.rooms()) {
            Configuration.write(paths.rooms.resolve(room.id().value() + extension), codec, MapProcessors.roomInfo(),
                    room);
        }

        for (DoorInfo door : data.doors()) {
            Configuration.write(paths.doors.resolve(door.id().value() + extension),
                    MapProcessors.doorInfo().elementFromData(door), codec);
        }

        for (ShopInfo shop : data.shops()) {
            Configuration.write(paths.shops.resolve(
                            getPositionString(shop.triggerLocation()) + "-" + shop.id().value() + extension),
                    MapProcessors.shopInfo().elementFromData(shop), codec);
        }

        List<WindowInfo> windows = data.windows();
        for (int i = 0; i < windows.size(); i++) {
            WindowInfo window = windows.get(i);
            Configuration.write(
                    paths.windows.resolve(getPositionString(window.frameRegion().origin()) + "-" + i + extension),
                    MapProcessors.windowInfo().elementFromData(window), codec);
        }

        for (RoundInfo round : data.rounds()) {
            Configuration.write(paths.rounds.resolve(round.round() + extension),
                    MapProcessors.roundInfo().elementFromData(round), codec);
        }

        for (SpawnruleInfo spawnrule : data.spawnrules()) {
            Configuration.write(paths.spawnrules.resolve(spawnrule.id().value() + extension),
                    MapProcessors.spawnruleInfo().elementFromData(spawnrule), codec);
        }

        List<SpawnpointInfo> spawnpoints = data.spawnpoints();
        for (int i = 0; i < spawnpoints.size(); i++) {
            SpawnpointInfo spawnpoint = spawnpoints.get(i);
            Configuration.write(
                    paths.spawnpoints.resolve(getPositionString(spawnpoint.position()) + "-" + i + extension),
                    MapProcessors.spawnpointInfo().elementFromData(spawnpoint), codec);
        }

        Configuration.write(paths.scoreboard.resolve("settings" + extension), data.scoreboard(), codec);
    }

    @Override
    public void delete(@NotNull String mapName) throws IOException {
        FileUtils.deleteRecursivelyIfExists(mapDirectoryFromName(mapName));
    }

    private Path mapDirectoryFromName(String mapName) throws IOException {
        return FileUtils.findFirstOrThrow(root, (path, attr) -> attr.isDirectory() && path.endsWith(mapName),
                () -> "Unable to find map folder for " + mapName);
    }

    private String getPositionString(Vec3I position) {
        return position.getX() + "_" + position.getY() + "_" + position.getZ();
    }

    private record FolderPaths(Path rooms,
                               Path doors,
                               Path shops,
                               Path windows,
                               Path rounds,
                               Path spawnrules,
                               Path spawnpoints,
                               Path scoreboard) {
        private FolderPaths(Path root) {
            this(root.resolve(ROOMS_PATH), root.resolve(DOORS_PATH), root.resolve(SHOPS_PATH),
                    root.resolve(WINDOWS_PATH), root.resolve(ROUNDS_PATH), root.resolve(SPAWNRULES_PATH),
                    root.resolve(SPAWNPOINTS_PATH), root.resolve(SCOREBOARD_PATH));
        }
    }
}
