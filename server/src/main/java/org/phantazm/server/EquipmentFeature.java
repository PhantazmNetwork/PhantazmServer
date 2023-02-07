package org.phantazm.server;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.dependency.ModuleDependencyProvider;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.bridge.Configuration;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.ElementUtils;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.core.equipment.EquipmentCreator;
import org.phantazm.zombies.equipment.gun.*;
import org.phantazm.zombies.equipment.gun.audience.EntityInstanceAudienceProvider;
import org.phantazm.zombies.equipment.gun.audience.PlayerAudienceProvider;
import org.phantazm.zombies.equipment.gun.data.GunData;
import org.phantazm.zombies.equipment.gun.effect.*;
import org.phantazm.zombies.equipment.gun.reload.StateReloadTester;
import org.phantazm.zombies.equipment.gun.reload.actionbar.GradientActionBarChooser;
import org.phantazm.zombies.equipment.gun.reload.actionbar.StaticActionBarChooser;
import org.phantazm.zombies.equipment.gun.shoot.StateShootTester;
import org.phantazm.zombies.equipment.gun.shoot.blockiteration.BasicBlockIteration;
import org.phantazm.zombies.equipment.gun.shoot.blockiteration.WallshotBlockIteration;
import org.phantazm.zombies.equipment.gun.shoot.blockiteration.WindowBlockIteration;
import org.phantazm.zombies.equipment.gun.shoot.endpoint.BasicShotEndpointSelector;
import org.phantazm.zombies.equipment.gun.shoot.fire.HitScanFirer;
import org.phantazm.zombies.equipment.gun.shoot.fire.SpreadFirer;
import org.phantazm.zombies.equipment.gun.shoot.fire.projectile.PhantazmMobProjectileCollisionFilter;
import org.phantazm.zombies.equipment.gun.shoot.fire.projectile.ProjectileFirer;
import org.phantazm.zombies.equipment.gun.shoot.handler.*;
import org.phantazm.zombies.equipment.gun.target.BasicTargetFinder;
import org.phantazm.zombies.equipment.gun.target.entityfinder.directional.AroundEndFinder;
import org.phantazm.zombies.equipment.gun.target.entityfinder.directional.BetweenPointsFinder;
import org.phantazm.zombies.equipment.gun.target.entityfinder.positional.NearbyEntityFinder;
import org.phantazm.zombies.equipment.gun.target.headshot.EyeHeightHeadshotTester;
import org.phantazm.zombies.equipment.gun.target.headshot.StaticHeadshotTester;
import org.phantazm.zombies.equipment.gun.target.intersectionfinder.RayTraceIntersectionFinder;
import org.phantazm.zombies.equipment.gun.target.intersectionfinder.StaticIntersectionFinder;
import org.phantazm.zombies.equipment.gun.target.limiter.DistanceTargetLimiter;
import org.phantazm.zombies.equipment.gun.target.tester.PhantazmMobTargetTester;
import org.phantazm.zombies.equipment.gun.visual.ClipStackMapper;
import org.phantazm.zombies.equipment.gun.visual.ReloadStackMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Main entrypoint for equipment features.
 */
final class EquipmentFeature {

    private static final Logger LOGGER = LoggerFactory.getLogger(EquipmentFeature.class);

    private static final Consumer<? super ElementException> HANDLER = ElementUtils.logging(LOGGER, "equipment");

    private static Map<Key, Pair<GunData, List<ElementContext>>> gunLevelMap = null;

    private static KeyParser keyParser = null;

    private EquipmentFeature() {
        throw new UnsupportedOperationException();
    }

    /**
     * Initialize equipment features.
     *
     * @param equipmentPath The {@link Path} to the equipment folder
     * @param codec         A {@link ConfigCodec} for serialization
     */
    static void initialize(@NotNull KeyParser keyParser, @NotNull ContextManager contextManager,
            @NotNull Path equipmentPath, @NotNull ConfigCodec codec,
            @NotNull ConfigProcessor<GunData> gunDataProcessor) {
        EquipmentFeature.keyParser = Objects.requireNonNull(keyParser, "keyParser");
        registerElementClasses(contextManager);

        String ending;
        if (codec.getPreferredExtensions().isEmpty()) {
            ending = "";
        }
        else {
            ending = "." + codec.getPreferredExtension();
        }
        PathMatcher matcher = equipmentPath.getFileSystem().getPathMatcher("glob:**" + ending);

        Path guns = equipmentPath.resolve("guns");
        try {
            Files.createDirectories(guns);
        }
        catch (IOException e) {
            LOGGER.warn("Failed to create guns directory.", e);
            return;
        }

        gunLevelMap = new HashMap<>();
        try (Stream<Path> gunDirectories = Files.list(guns)) {
            for (Path gunDirectory : (Iterable<? extends Path>)gunDirectories::iterator) {
                if (!Files.isDirectory(gunDirectory)) {
                    continue;
                }

                String infoFileName = codec.getPreferredExtensions().isEmpty()
                                      ? "settings"
                                      : "settings." + codec.getPreferredExtension();
                Path infoPath = gunDirectory.resolve(infoFileName);
                if (!Files.isRegularFile(infoPath)) {
                    LOGGER.warn("No gun settings file at {}.", infoPath);
                    continue;
                }

                GunData gunData;
                try {
                    gunData = Configuration.read(infoPath, codec, gunDataProcessor);
                }
                catch (ConfigProcessException e) {
                    LOGGER.warn("Failed to read gun settings file at {}.", infoPath, e);
                    continue;
                }

                List<ElementContext> levelData = new ArrayList<>();
                Path levelsPath = gunDirectory.resolve("levels");
                try (Stream<Path> levelDirectories = Files.list(levelsPath)) {
                    for (Path levelFile : (Iterable<? extends Path>)levelDirectories::iterator) {
                        if (!(Files.isRegularFile(levelFile) && matcher.matches(levelFile))) {
                            continue;
                        }

                        try {
                            ConfigNode node = Configuration.read(levelFile, codec, ConfigProcessor.CONFIG_NODE);
                            levelData.add(contextManager.makeContext(node));
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

                gunLevelMap.put(gunData.name(), Pair.of(gunData, levelData));
            }
        }
        catch (IOException e) {
            LOGGER.warn("Failed to list guns directory at {}", guns, e);
        }

        LOGGER.info("Loaded {} guns.", gunLevelMap.size());
    }

    private static void registerElementClasses(@NotNull ContextManager contextManager) {
        LOGGER.info("Registering Equipment element classes...");

        contextManager.registerElementClass(GunLevel.class);
        contextManager.registerElementClass(EntityInstanceAudienceProvider.class);
        contextManager.registerElementClass(PlayerAudienceProvider.class);
        contextManager.registerElementClass(AmmoLevelEffect.class);
        contextManager.registerElementClass(PlaySoundEffect.class);
        contextManager.registerElementClass(ReloadActionBarEffect.class);
        contextManager.registerElementClass(SendMessageEffect.class);
        contextManager.registerElementClass(ShootExpEffect.class);
        contextManager.registerElementClass(GradientActionBarChooser.class);
        contextManager.registerElementClass(StaticActionBarChooser.class);
        contextManager.registerElementClass(StateReloadTester.class);
        contextManager.registerElementClass(BasicShotEndpointSelector.class);
        contextManager.registerElementClass(BasicBlockIteration.class);
        contextManager.registerElementClass(WallshotBlockIteration.class);
        contextManager.registerElementClass(WindowBlockIteration.class);
        contextManager.registerElementClass(PhantazmMobProjectileCollisionFilter.class);
        contextManager.registerElementClass(ProjectileFirer.class);
        contextManager.registerElementClass(HitScanFirer.class);
        contextManager.registerElementClass(SpreadFirer.class);
        contextManager.registerElementClass(ChainShotHandler.class);
        contextManager.registerElementClass(DamageShotHandler.class);
        contextManager.registerElementClass(GiveCoinsShotHandler.class);
        contextManager.registerElementClass(ExplosionShotHandler.class);
        contextManager.registerElementClass(GuardianBeamShotHandler.class);
        contextManager.registerElementClass(IgniteShotHandler.class);
        contextManager.registerElementClass(KnockbackShotHandler.class);
        contextManager.registerElementClass(MessageShotHandler.class);
        contextManager.registerElementClass(ParticleTrailShotHandler.class);
        contextManager.registerElementClass(PotionShotHandler.class);
        contextManager.registerElementClass(SlowDownShotHandler.class);
        contextManager.registerElementClass(SoundShotHandler.class);
        contextManager.registerElementClass(StateShootTester.class);
        contextManager.registerElementClass(AroundEndFinder.class);
        contextManager.registerElementClass(BetweenPointsFinder.class);
        contextManager.registerElementClass(NearbyEntityFinder.class);
        contextManager.registerElementClass(EyeHeightHeadshotTester.class);
        contextManager.registerElementClass(StaticHeadshotTester.class);
        contextManager.registerElementClass(RayTraceIntersectionFinder.class);
        contextManager.registerElementClass(StaticIntersectionFinder.class);
        contextManager.registerElementClass(DistanceTargetLimiter.class);
        contextManager.registerElementClass(PhantazmMobTargetTester.class);
        contextManager.registerElementClass(BasicTargetFinder.class);
        contextManager.registerElementClass(ClipStackMapper.class);
        contextManager.registerElementClass(ReloadStackMapper.class);
        contextManager.registerElementClass(GunStats.class);

        LOGGER.info("Registered Equipment element classes.");
    }

    public static @NotNull EquipmentCreator createEquipmentCreator(@NotNull ZombiesGunModule gunModule) {
        Objects.requireNonNull(gunModule, "gunModule");
        FeatureUtils.check(gunLevelMap);
        FeatureUtils.check(keyParser);

        DependencyProvider provider = new ModuleDependencyProvider(keyParser, gunModule);

        return new EquipmentCreator() {
            @Override
            public boolean hasEquipment(@NotNull Key equipmentKey) {
                return gunLevelMap.containsKey(equipmentKey);
            }

            @SuppressWarnings("unchecked")
            @NotNull
            @Override
            public <TEquipment extends Equipment> Optional<TEquipment> createEquipment(@NotNull Key equipmentKey) {
                Pair<GunData, List<ElementContext>> pair = gunLevelMap.get(equipmentKey);
                if (pair == null) {
                    return Optional.empty();
                }

                Map<Key, GunLevel> levels = new HashMap<>(pair.right().size());
                Key rootLevel = pair.left().rootLevel();
                for (ElementContext context : pair.right()) {
                    GunLevel level = context.provide(provider, HANDLER, () -> null);
                    if (level != null) {
                        levels.put(level.data().key(), level);
                    }
                }

                if (levels.isEmpty()) {
                    return Optional.empty();
                }

                GunModel model = new GunModel(rootLevel, levels);
                Gun gun = new Gun(gunModule.getPlayerView()::getPlayer, model);

                return Optional.of((TEquipment)gun);
            }
        };
    }

}
