package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.api.config.processor.ItemStackConfigProcessors;
import com.github.phantazmnetwork.commons.config.ComplexData;
import com.github.phantazmnetwork.commons.config.ComplexDataConfigProcessor;
import com.github.phantazmnetwork.zombies.equipment.gun.data.GunLevelDataConfigProcessor;
import com.github.phantazmnetwork.zombies.equipment.gun.GunStats;
import com.github.phantazmnetwork.zombies.equipment.gun.data.GunData;
import com.github.phantazmnetwork.zombies.equipment.gun.data.GunLevelData;
import com.github.phantazmnetwork.zombies.equipment.gun.effect.*;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.StateReloadTester;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.actionbar.GradientActionBarChooser;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.actionbar.StaticActionBarChooser;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.StateShootTester;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint.BasicShotEndpointSelector;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint.RayTraceBlockIteration;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint.WallshotBlockIteration;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.HitScanFirer;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.ProjectileFirer;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.SpreadFirer;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler.*;
import com.github.phantazmnetwork.zombies.equipment.gun.target.BasicTargetFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.directional.AroundEndFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.directional.BetweenPointsFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.positional.NearbyPhantazmMobFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.headshot.EyeHeightHeadshotTester;
import com.github.phantazmnetwork.zombies.equipment.gun.target.headshot.StaticHeadshotTester;
import com.github.phantazmnetwork.zombies.equipment.gun.target.intersectionfinder.RayTraceIntersectionFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.intersectionfinder.StaticIntersectionFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.visual.ClipStackMapper;
import com.github.phantazmnetwork.zombies.equipment.gun.visual.ReloadStackMapper;
import com.github.steanky.ethylene.core.bridge.ConfigBridges;
import com.github.steanky.ethylene.core.codec.ConfigCodec;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

final class EquipmentFeature {

    private static final Logger LOGGER = LoggerFactory.getLogger(EquipmentFeature.class);

    private static Map<Key, List<ComplexData>> gunLevelMap = null;

    private EquipmentFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull Path equipmentPath, @NotNull ConfigCodec codec) {
        PathMatcher matcher = equipmentPath.getFileSystem()
                .getPathMatcher("glob:**." + codec.getPreferredExtension());

        Path guns = equipmentPath.resolve("guns");
        try {
            Files.createDirectories(guns);
        } catch (IOException e) {
            LOGGER.warn("Failed to create guns directory.", e);
            return;
        }

        ConfigProcessor<GunData> gunDataProcessor = GunData.processor();
        ConfigProcessor<ComplexData> gunLevelProcessor = createGunLevelProcessor();
        Map<Key, BiConsumer<? extends Keyed, Collection<Key>>> dependencyAdders = createDependencyAdders();

        gunLevelMap = new HashMap<>();
        try (Stream<Path> gunDirectories = Files.list(guns)) {
            gunIteration: for (Path gunDirectory : (Iterable<? extends Path>) gunDirectories::iterator) {
                if (!Files.isDirectory(gunDirectory)) {
                    continue;
                }

                Path infoPath = gunDirectory.resolve("info." + codec.getPreferredExtension());
                if (!Files.isRegularFile(infoPath)) {
                    LOGGER.warn("No info file at {}.", infoPath);
                    continue;
                }

                GunData gunData;
                try {
                    gunData = ConfigBridges.read(infoPath, codec, gunDataProcessor);
                } catch (ConfigProcessException e) {
                    LOGGER.warn("Failed to read info file at {}.", infoPath, e);
                    continue;
                }

                Int2ObjectMap<ComplexData> levelDataMap = new Int2ObjectOpenHashMap<>();
                Path levelsPath = gunDirectory.resolve("levels");
                try (Stream<Path> levelDirectories = Files.list(levelsPath)) {
                    for (Path levelFile : (Iterable<? extends Path>) levelDirectories::iterator) {
                        if (!(Files.isRegularFile(levelFile) && matcher.matches(levelFile))) {
                            continue;
                        }

                        try {
                            ComplexData data = ConfigBridges.read(levelFile, codec, gunLevelProcessor);

                            Set<Key> required = new HashSet<>();
                            for (Keyed object : data.objects().values()) {
                                BiConsumer<? extends Keyed, Collection<Key>> dependencyAdder
                                        = dependencyAdders.get(object.key());
                                if (dependencyAdder != null) {
                                    invokeDependencyAdder(dependencyAdder, object, required);
                                }
                            }
                            required.removeAll(data.objects().keySet());

                            if (!required.isEmpty()) {
                                LOGGER.warn("Invalid gun level at {}. Missing required keys: {}", levelFile, required);
                                continue;
                            }

                            Keyed mainObject = data.objects().get(data.mainKey());
                            if (!(mainObject instanceof GunLevelData gunLevelData)) {
                                LOGGER.warn("Invalid gun level at {}. No gun level data.", levelFile);
                                continue;
                            }

                            int order = gunLevelData.order();
                            levelDataMap.put(order, data);
                        }
                        catch (IOException e) {
                            LOGGER.warn("Failed to read level file at {}.", levelFile, e);
                        }
                    }
                }
                catch (IOException e) {
                    LOGGER.warn("Failed to read levels directory at {}.", levelsPath, e);
                    continue;
                }

                int maxOrder = -1;
                for (int order : levelDataMap.keySet()) {
                    if (order > maxOrder) {
                        maxOrder = order;
                    }
                }

                List<ComplexData> levelData = new ArrayList<>(levelDataMap.size());
                for (int i = 0; i <= maxOrder; i++) {
                    ComplexData data = levelDataMap.get(i);
                    if (data != null) {
                        levelData.add(data);
                    }
                    else {
                        LOGGER.warn("Missing level {} for gun {}.", i, gunData.name());
                        continue gunIteration;
                    }
                }

                gunLevelMap.put(gunData.name(), levelData);
            }
        }
        catch (IOException e) {
            LOGGER.warn("Failed to list guns directory at {}", guns, e);
        }

        LOGGER.info("Loaded {} guns.", gunLevelMap.size());
    }

    private static @NotNull ConfigProcessor<ComplexData> createGunLevelProcessor() {
        Map<Key, ConfigProcessor<? extends Keyed>> gunProcessors = new HashMap<>(37);
        gunProcessors.put(GunStats.SERIAL_KEY, GunStats.processor());
        gunProcessors.put(GunLevelData.SERIAL_KEY, new GunLevelDataConfigProcessor(ItemStackConfigProcessors.snbt()));
        gunProcessors.put(AmmoLevelEffect.Data.SERIAL_KEY, AmmoLevelEffect.processor());
        gunProcessors.put(PlaySoundEffect.Data.SERIAL_KEY, PlaySoundEffect.processor());
        gunProcessors.put(ReloadActionBarEffect.Data.SERIAL_KEY, ReloadActionBarEffect.processor());
        gunProcessors.put(SendMessageEffect.Data.SERIAL_KEY, SendMessageEffect.processor());
        gunProcessors.put(ShootExpEffect.Data.SERIAL_KEY, ShootExpEffect.processor());
        gunProcessors.put(GradientActionBarChooser.Data.SERIAL_KEY, GradientActionBarChooser.processor());
        gunProcessors.put(StaticActionBarChooser.Data.SERIAL_KEY, StaticActionBarChooser.processor());
        gunProcessors.put(StateReloadTester.Data.SERIAL_KEY, StateReloadTester.processor());
        gunProcessors.put(BasicShotEndpointSelector.Data.SERIAL_KEY, BasicShotEndpointSelector.processor());
        gunProcessors.put(RayTraceBlockIteration.Data.SERIAL_KEY, RayTraceBlockIteration.processor());
        gunProcessors.put(WallshotBlockIteration.Data.SERIAL_KEY, WallshotBlockIteration.processor());
        gunProcessors.put(HitScanFirer.Data.SERIAL_KEY, HitScanFirer.processor());
        gunProcessors.put(ProjectileFirer.Data.SERIAL_KEY, ProjectileFirer.processor());
        gunProcessors.put(SpreadFirer.Data.SERIAL_KEY, SpreadFirer.processor());
        gunProcessors.put(ChainShotHandler.Data.SERIAL_KEY, ChainShotHandler.processor());
        gunProcessors.put(DamageShotHandler.Data.SERIAL_KEY, DamageShotHandler.processor());
        gunProcessors.put(ExplosionShotHandler.Data.SERIAL_KEY, ExplosionShotHandler.processor());
        gunProcessors.put(FeedbackShotHandler.Data.SERIAL_KEY, FeedbackShotHandler.processor());
        gunProcessors.put(GuardianBeamShotHandler.Data.SERIAL_KEY, GuardianBeamShotHandler.processor());
        gunProcessors.put(IgniteShotHandler.Data.SERIAL_KEY, IgniteShotHandler.processor());
        gunProcessors.put(KnockbackShotHandler.Data.SERIAL_KEY, KnockbackShotHandler.processor());
        gunProcessors.put(ParticleTrailShotHandler.Data.SERIAL_KEY, ParticleTrailShotHandler.processor());
        gunProcessors.put(PotionShotHandler.Data.SERIAL_KEY, PotionShotHandler.processor());
        gunProcessors.put(SoundShotHandler.Data.SERIAL_KEY, SoundShotHandler.processor());
        gunProcessors.put(StateShootTester.Data.SERIAL_KEY, StateShootTester.processor());
        gunProcessors.put(AroundEndFinder.Data.SERIAL_KEY, AroundEndFinder.processor());
        gunProcessors.put(BetweenPointsFinder.Data.SERIAL_KEY, BetweenPointsFinder.processor());
        gunProcessors.put(NearbyPhantazmMobFinder.Data.SERIAL_KEY, NearbyPhantazmMobFinder.processor());
        gunProcessors.put(EyeHeightHeadshotTester.Data.SERIAL_KEY, EyeHeightHeadshotTester.processor());
        gunProcessors.put(StaticHeadshotTester.Data.SERIAL_KEY, StaticHeadshotTester.processor());
        gunProcessors.put(RayTraceIntersectionFinder.Data.SERIAL_KEY, RayTraceIntersectionFinder.processor());
        gunProcessors.put(StaticIntersectionFinder.Data.SERIAL_KEY, StaticIntersectionFinder.processor());
        gunProcessors.put(BasicTargetFinder.Data.SERIAL_KEY, BasicTargetFinder.processor());
        gunProcessors.put(ClipStackMapper.Data.SERIAL_KEY, ClipStackMapper.processor());
        gunProcessors.put(ReloadStackMapper.Data.SERIAL_KEY, ReloadStackMapper.processor());
        return new ComplexDataConfigProcessor(gunProcessors);
    }

    private static @NotNull Map<Key, BiConsumer<? extends Keyed, Collection<Key>>> createDependencyAdders() {
        Map<Key, BiConsumer<? extends Keyed, Collection<Key>>> dependencyAdders = new HashMap<>(13);
        dependencyAdders.put(GunLevelData.SERIAL_KEY, GunLevelData.dependencyConsumer());
        dependencyAdders.put(ReloadActionBarEffect.Data.SERIAL_KEY, ReloadActionBarEffect.dependencyConsumer());
        dependencyAdders.put(ShootExpEffect.Data.SERIAL_KEY, ShootExpEffect.dependencyConsumer());
        dependencyAdders.put(StateReloadTester.Data.SERIAL_KEY, StateReloadTester.dependencyConsumer());
        dependencyAdders.put(StateShootTester.Data.SERIAL_KEY, StateShootTester.dependencyConsumer());
        dependencyAdders.put(BasicShotEndpointSelector.Data.SERIAL_KEY, BasicShotEndpointSelector.dependencyConsumer());
        dependencyAdders.put(HitScanFirer.Data.SERIAL_KEY, HitScanFirer.dependencyConsumer());
        dependencyAdders.put(ProjectileFirer.Data.SERIAL_KEY, ProjectileFirer.dependencyConsumer());
        dependencyAdders.put(SpreadFirer.Data.SERIAL_KEY, SpreadFirer.dependencyConsumer());
        dependencyAdders.put(ChainShotHandler.Data.SERIAL_KEY, ChainShotHandler.dependencyConsumer());
        dependencyAdders.put(BasicTargetFinder.Data.SERIAL_KEY, BasicTargetFinder.dependencyConsumer());
        dependencyAdders.put(ClipStackMapper.Data.SERIAL_KEY, ClipStackMapper.dependencyConsumer());
        dependencyAdders.put(ReloadStackMapper.Data.SERIAL_KEY, ReloadStackMapper.dependencyConsumer());

        return dependencyAdders;
    }

    @SuppressWarnings("unchecked")
    private static <TObject extends Keyed> void invokeDependencyAdder(@NotNull BiConsumer<TObject, Collection<Key>> dependencyAdder,
                                                                      @NotNull Keyed object,
                                                                      @NotNull Collection<Key> dependencies) {
        dependencyAdder.accept((TObject) object, dependencies);
    }

    public static @NotNull Map<Key, List<ComplexData>> getGunLevelMap() {
        return requireInitialized(gunLevelMap);
    }

    private static <TObject> @NotNull TObject requireInitialized(TObject object) {
        if (object == null) {
            throw new IllegalStateException("EquipmentFeature has not been initialized yet");
        }

        return object;
    }

}
