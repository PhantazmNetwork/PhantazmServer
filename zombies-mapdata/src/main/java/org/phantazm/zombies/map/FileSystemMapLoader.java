package org.phantazm.zombies.map;

import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.bridge.Configuration;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import com.github.steanky.ethylene.mapper.type.Token;
import com.github.steanky.vector.Bounds3I;
import com.github.steanky.vector.Vec3I;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

/**
 * A {@link Loader} implementation that loads maps from a filesystem.
 */
public class FileSystemMapLoader extends FilesystemLoader<MapInfo> {
    private static final String ROOMS_PATH = "rooms";
    private static final String DOORS_PATH = "doors";
    private static final String SHOPS_PATH = "shops";
    private static final String WINDOWS_PATH = "windows";
    private static final String ROUNDS_PATH = "rounds";
    private static final String SPAWNRULES_PATH = "spawnrules";
    private static final String SPAWNPOINTS_PATH = "spawnpoints";

    private final String mapInfoName;
    private final BiPredicate<Path, BasicFileAttributes> configPredicate;
    private final ConfigCodec codec;
    private final MappingProcessorSource mappingProcessorSource;

    /**
     * Constructs a new instance of this class from the provided root path and using the provided {@link ConfigCodec} to
     * serialize/deserialize map data files.
     *
     * @param root  the root path from which to search for map information
     * @param codec the codec used to serialize/deserialize map data files
     */
    public FileSystemMapLoader(@NotNull Path root, @NotNull ConfigCodec codec,
            @NotNull MappingProcessorSource mappingProcessorSource) {
        super(root);
        this.codec = Objects.requireNonNull(codec, "codec");
        this.mappingProcessorSource = Objects.requireNonNull(mappingProcessorSource, "mappingProcessorSource");

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

        FolderPaths paths = new FolderPaths(mapDirectory);
        List<RoomInfo> rooms = new ArrayList<>();
        List<DoorInfo> doors = new ArrayList<>();
        List<ShopInfo> shops = new ArrayList<>();
        List<WindowInfo> windows = new ArrayList<>();
        List<RoundInfo> rounds = new ArrayList<>();
        List<SpawnruleInfo> spawnrules = new ArrayList<>();
        List<SpawnpointInfo> spawnpoints = new ArrayList<>();

        FileUtils.forEachFileMatching(paths.rooms, configPredicate, file -> rooms.add(
                Configuration.read(file, codec, mappingProcessorSource.processorFor(Token.ofClass(RoomInfo.class)))));

        FileUtils.forEachFileMatching(paths.doors, configPredicate, file -> doors.add(
                Configuration.read(file, codec, mappingProcessorSource.processorFor(Token.ofClass(DoorInfo.class)))));

        FileUtils.forEachFileMatching(paths.shops, configPredicate, file -> shops.add(
                Configuration.read(file, codec, mappingProcessorSource.processorFor(Token.ofClass(ShopInfo.class)))));

        FileUtils.forEachFileMatching(paths.windows, configPredicate, file -> windows.add(
                Configuration.read(file, codec, mappingProcessorSource.processorFor(Token.ofClass(WindowInfo.class)))));

        FileUtils.forEachFileMatching(paths.rounds, configPredicate, file -> rounds.add(
                Configuration.read(file, codec, mappingProcessorSource.processorFor(Token.ofClass(RoundInfo.class)))));
        rounds.sort(Comparator.comparingInt(RoundInfo::round));

        FileUtils.forEachFileMatching(paths.spawnrules, configPredicate, file -> spawnrules.add(
                Configuration.read(file, codec,
                        mappingProcessorSource.processorFor(Token.ofClass(SpawnruleInfo.class)))));

        FileUtils.forEachFileMatching(paths.spawnpoints, configPredicate, file -> spawnpoints.add(
                Configuration.read(file, codec,
                        mappingProcessorSource.processorFor(Token.ofClass(SpawnpointInfo.class)))));

        String sidebarSettingsPath =
                "sidebar" + (codec.getPreferredExtensions().isEmpty() ? "" : "." + codec.getPreferredExtension());
        ConfigNode scoreboard = Configuration.read(mapDirectory.resolve(sidebarSettingsPath), codec).asNode();

        String corpsePath =
                "corpse" + (codec.getPreferredExtensions().isEmpty() ? "" : "." + codec.getPreferredExtension());
        ConfigNode corpse = Configuration.read(mapDirectory.resolve(corpsePath), codec).asNode();

        String coinsPath =
                "coins" + (codec.getPreferredExtensions().isEmpty() ? "" : "." + codec.getPreferredExtension());
        PlayerCoinsInfo playerCoins = Configuration.read(mapDirectory.resolve(coinsPath), codec,
                mappingProcessorSource.processorFor(Token.ofClass(PlayerCoinsInfo.class)));

        String leaderboardPath =
                "leaderboard" + (codec.getPreferredExtensions().isEmpty() ? "" : "." + codec.getPreferredExtension());
        LeaderboardInfo leaderboard = Configuration.read(mapDirectory.resolve(leaderboardPath), codec,
                mappingProcessorSource.processorFor(Token.ofClass(LeaderboardInfo.class)));

        String webhookPath =
                "webhook" + (codec.getPreferredExtensions().isEmpty() ? "" : "." + codec.getPreferredExtension());
        WebhookInfo webhook = Configuration.read(mapDirectory.resolve(webhookPath), codec,
                mappingProcessorSource.processorFor(Token.ofClass(WebhookInfo.class)));


        MapSettingsInfo mapSettingsInfo = Configuration.read(mapInfoFile, codec,
                mappingProcessorSource.processorFor(Token.ofClass(MapSettingsInfo.class)));

        return new MapInfo(mapSettingsInfo, playerCoins, rooms, doors, shops, windows, rounds, spawnrules, spawnpoints,
                leaderboard, scoreboard, corpse, webhook);
    }

    @Override
    public void save(@NotNull MapInfo data) throws IOException {
        Path mapDirectory = root.resolve(data.settings().id().value());
        FileUtils.createDirectories(mapDirectory);

        MapSettingsInfo mapSettingsInfo = data.settings();
        Configuration.write(mapDirectory.resolve(mapInfoName),
                mappingProcessorSource.processorFor(Token.ofClass(MapSettingsInfo.class))
                        .elementFromData(mapSettingsInfo), codec);

        FolderPaths paths = new FolderPaths(mapDirectory);

        FileUtils.deleteRecursivelyIfExists(paths.rooms);
        FileUtils.deleteRecursivelyIfExists(paths.doors);
        FileUtils.deleteRecursivelyIfExists(paths.shops);
        FileUtils.deleteRecursivelyIfExists(paths.windows);
        FileUtils.deleteRecursivelyIfExists(paths.rounds);
        FileUtils.deleteRecursivelyIfExists(paths.spawnrules);
        FileUtils.deleteRecursivelyIfExists(paths.spawnpoints);

        FileUtils.createDirectories(paths.rooms);
        FileUtils.createDirectories(paths.doors);
        FileUtils.createDirectories(paths.shops);
        FileUtils.createDirectories(paths.windows);
        FileUtils.createDirectories(paths.rounds);
        FileUtils.createDirectories(paths.spawnrules);
        FileUtils.createDirectories(paths.spawnpoints);

        String extension = codec.getPreferredExtensions().isEmpty() ? "" : "." + codec.getPreferredExtension();
        for (RoomInfo room : data.rooms()) {
            Configuration.write(paths.rooms.resolve(room.id().value() + extension), codec,
                    mappingProcessorSource.processorFor(Token.ofClass(RoomInfo.class)), room);
        }

        for (DoorInfo door : data.doors()) {
            Configuration.write(paths.doors.resolve(door.id().value() + extension),
                    mappingProcessorSource.processorFor(Token.ofClass(DoorInfo.class)).elementFromData(door), codec);
        }

        for (ShopInfo shop : data.shops()) {
            Bounds3I trigger = shop.trigger();
            Vec3I origin = Vec3I.immutable(trigger.originX(), trigger.originY(), trigger.originZ());
            Configuration.write(paths.shops.resolve(getPositionString(origin) + "-" + shop.id().value() + extension),
                    mappingProcessorSource.processorFor(Token.ofClass(ShopInfo.class)).elementFromData(shop), codec);
        }

        List<WindowInfo> windows = data.windows();
        for (int i = 0; i < windows.size(); i++) {
            WindowInfo window = windows.get(i);
            Bounds3I frameRegion = window.frameRegion();
            Vec3I origin = Vec3I.immutable(frameRegion.originX(), frameRegion.originY(), frameRegion.originZ());
            Configuration.write(paths.windows.resolve(getPositionString(origin) + "-" + i + extension),
                    mappingProcessorSource.processorFor(Token.ofClass(WindowInfo.class)).elementFromData(window),
                    codec);
        }

        for (RoundInfo round : data.rounds()) {
            Configuration.write(paths.rounds.resolve(round.round() + extension),
                    mappingProcessorSource.processorFor(Token.ofClass(RoundInfo.class)).elementFromData(round), codec);
        }

        for (SpawnruleInfo spawnrule : data.spawnrules()) {
            Configuration.write(paths.spawnrules.resolve(spawnrule.id().value() + extension),
                    mappingProcessorSource.processorFor(Token.ofClass(SpawnruleInfo.class)).elementFromData(spawnrule),
                    codec);
        }

        List<SpawnpointInfo> spawnpoints = data.spawnpoints();
        for (int i = 0; i < spawnpoints.size(); i++) {
            SpawnpointInfo spawnpoint = spawnpoints.get(i);
            Configuration.write(
                    paths.spawnpoints.resolve(getPositionString(spawnpoint.position()) + "-" + i + extension),
                    mappingProcessorSource.processorFor(Token.ofClass(SpawnpointInfo.class))
                            .elementFromData(spawnpoint), codec);
        }

        Configuration.write(mapDirectory.resolve("sidebar" + extension), data.scoreboard(), codec);
        Configuration.write(mapDirectory.resolve("corpse" + extension), data.corpse(), codec);
    }

    @Override
    public void delete(@NotNull String mapName) throws IOException {
        FileUtils.deleteRecursivelyIfExists(mapDirectoryFromName(mapName));
    }

    @Override
    public @NotNull @Unmodifiable List<String> loadableData() throws IOException {
        if (!Files.exists(root)) {
            FileUtils.createDirectories(root);
        }

        try (Stream<Path> fileStream = Files.list(root)) {
            return fileStream.filter(Files::isDirectory).map(path -> path.getFileName().toString()).toList();
        }
    }

    private Path mapDirectoryFromName(String mapName) throws IOException {
        return FileUtils.findFirstOrThrow(root, (path, attr) -> attr.isDirectory() && path.endsWith(mapName),
                () -> "Unable to find map folder for " + mapName);
    }

    private String getPositionString(Vec3I position) {
        return position.x() + "_" + position.y() + "_" + position.z();
    }

    private record FolderPaths(Path rooms,
                               Path doors,
                               Path shops,
                               Path windows,
                               Path rounds,
                               Path spawnrules,
                               Path spawnpoints) {
        private FolderPaths(Path root) {
            this(root.resolve(ROOMS_PATH), root.resolve(DOORS_PATH), root.resolve(SHOPS_PATH),
                    root.resolve(WINDOWS_PATH), root.resolve(ROUNDS_PATH), root.resolve(SPAWNRULES_PATH),
                    root.resolve(SPAWNPOINTS_PATH));
        }
    }
}
