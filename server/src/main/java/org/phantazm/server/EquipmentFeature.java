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
import org.phantazm.zombies.equipment.EquipmentData;
import org.phantazm.zombies.equipment.EquipmentTypes;
import org.phantazm.zombies.equipment.gun.*;
import org.phantazm.zombies.equipment.gun.audience.EntityInstanceAudienceProvider;
import org.phantazm.zombies.equipment.gun.audience.PlayerAudienceProvider;
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
import org.phantazm.zombies.equipment.perk.BasicPerkCreator;
import org.phantazm.zombies.equipment.perk.PerkCreator;
import org.phantazm.zombies.equipment.perk.effect.AddGroupSlotsCreator;
import org.phantazm.zombies.equipment.perk.effect.FlaggingPerkEffectCreator;
import org.phantazm.zombies.equipment.perk.effect.ModifierPerkEffectCreator;
import org.phantazm.zombies.equipment.perk.effect.ShotEffectCreator;
import org.phantazm.zombies.equipment.perk.effect.shot.ApplyAttributeShotEffect;
import org.phantazm.zombies.equipment.perk.effect.shot.ApplyFireShotEffect;
import org.phantazm.zombies.equipment.perk.equipment.BasicPerkEquipmentCreator;
import org.phantazm.zombies.equipment.perk.equipment.interactor.CooldownInteractorCreator;
import org.phantazm.zombies.equipment.perk.equipment.interactor.MeleeInteractorCreator;
import org.phantazm.zombies.equipment.perk.equipment.interactor.NoInteractorCreator;
import org.phantazm.zombies.equipment.perk.equipment.visual.StaticVisualCreator;
import org.phantazm.zombies.equipment.perk.level.NonUpgradeablePerkLevelCreator;
import org.phantazm.zombies.equipment.perk.level.PerkLevelCreator;
import org.phantazm.zombies.equipment.perk.level.UpgradeablePerkLevelCreator;
import org.phantazm.zombies.player.ZombiesPlayer;
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

    private static final Path EQUIPMENT_PATH = Path.of("./equipment/");

    private static Map<Key, Pair<EquipmentData, List<ElementContext>>> equipmentLevelMap;
    private static KeyParser keyParser = null;

    private EquipmentFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull KeyParser keyParser, @NotNull ContextManager contextManager,
            @NotNull ConfigCodec codec, @NotNull ConfigProcessor<EquipmentData> gunDataProcessor) {
        EquipmentFeature.keyParser = Objects.requireNonNull(keyParser, "keyParser");
        registerElementClasses(contextManager);

        String ending;
        if (codec.getPreferredExtensions().isEmpty()) {
            ending = "";
        }
        else {
            ending = "." + codec.getPreferredExtension();
        }

        PathMatcher matcher = EQUIPMENT_PATH.getFileSystem().getPathMatcher("glob:**" + ending);

        Path guns = EQUIPMENT_PATH.resolve("guns");
        Path perks = EQUIPMENT_PATH.resolve("perks");
        try {
            Files.createDirectories(guns);
            Files.createDirectories(perks);
        }
        catch (IOException e) {
            LOGGER.warn("Failed to create a necessary equipment directory.", e);
            return;
        }

        List<Path> equipmentDirectories = List.of(guns, perks);
        Map<Key, Pair<EquipmentData, List<ElementContext>>> equipmentLevelMap = new HashMap<>();

        for (Path equipmentDirectory : equipmentDirectories) {
            try (Stream<Path> gunDirectories = Files.list(equipmentDirectory)) {
                for (Path gunDirectory : (Iterable<? extends Path>)gunDirectories::iterator) {
                    if (!Files.isDirectory(gunDirectory)) {
                        continue;
                    }

                    String infoFileName = codec.getPreferredExtensions().isEmpty()
                                          ? "settings"
                                          : "settings." + codec.getPreferredExtension();
                    Path infoPath = gunDirectory.resolve(infoFileName);
                    if (!Files.isRegularFile(infoPath)) {
                        LOGGER.warn("No equipment settings file at {}.", infoPath);
                        continue;
                    }

                    EquipmentData equipmentData;
                    try {
                        equipmentData = Configuration.read(infoPath, codec, gunDataProcessor);
                    }
                    catch (ConfigProcessException e) {
                        LOGGER.warn("Failed to read equipment settings file at {}.", infoPath, e);
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
                        LOGGER.warn("Failed to list levels directory at {}.", levelsPath, e);
                        continue;
                    }

                    equipmentLevelMap.put(equipmentData.name(), Pair.of(equipmentData, levelData));
                }
            }
            catch (IOException e) {
                LOGGER.warn("Failed to list equipment directory at {}", guns, e);
            }
        }

        EquipmentFeature.equipmentLevelMap = Map.copyOf(equipmentLevelMap);
        LOGGER.info("Loaded {} equipment.", equipmentLevelMap.size());
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

        //PerkEffectCreators
        contextManager.registerElementClass(FlaggingPerkEffectCreator.class);
        contextManager.registerElementClass(ModifierPerkEffectCreator.class);
        contextManager.registerElementClass(AddGroupSlotsCreator.class);
        contextManager.registerElementClass(ShotEffectCreator.class);

        //ShotEffects
        contextManager.registerElementClass(ApplyAttributeShotEffect.class);
        contextManager.registerElementClass(ApplyFireShotEffect.class);

        //PerkInteractorCreators
        contextManager.registerElementClass(CooldownInteractorCreator.class);
        contextManager.registerElementClass(MeleeInteractorCreator.class);
        contextManager.registerElementClass(NoInteractorCreator.class);

        //PerkVisualCreators
        contextManager.registerElementClass(StaticVisualCreator.class);

        //PerkEquipmentCreators
        contextManager.registerElementClass(BasicPerkEquipmentCreator.class);

        //PerkLevelCreators
        contextManager.registerElementClass(NonUpgradeablePerkLevelCreator.class);
        contextManager.registerElementClass(UpgradeablePerkLevelCreator.class);

        LOGGER.info("Registered Equipment element classes.");
    }

    public static @NotNull EquipmentCreator createEquipmentCreator(@NotNull ZombiesEquipmentModule equipmentModule) {
        Objects.requireNonNull(equipmentModule, "equipmentModule");
        FeatureUtils.check(equipmentLevelMap);
        FeatureUtils.check(keyParser);

        DependencyProvider provider = new ModuleDependencyProvider(keyParser, equipmentModule);

        Map<Key, PerkCreator> perkCreatorMap = new HashMap<>();
        for (Map.Entry<Key, Pair<EquipmentData, List<ElementContext>>> equipmentEntry : equipmentLevelMap.entrySet()) {
            Pair<EquipmentData, List<ElementContext>> pair = equipmentEntry.getValue();
            EquipmentData data = pair.first();

            if (!EquipmentTypes.PERK.equals(pair.left().type())) {
                continue;
            }

            Map<Key, PerkLevelCreator> perkLevels = new HashMap<>(pair.right().size());
            for (ElementContext context : pair.right()) {
                PerkLevelCreator perkLevelCreator = context.provide(provider, HANDLER, () -> null);
                if (perkLevelCreator != null) {
                    perkLevels.put(perkLevelCreator.levelKey(), perkLevelCreator);
                }
            }

            if (!perkLevels.containsKey(data.rootLevel())) {
                LOGGER.warn("Perk {} does not contain root level {}", data.name(), data.rootLevel());
                continue;
            }

            Key equipmentName = data.name();
            perkCreatorMap.put(equipmentName, new BasicPerkCreator(equipmentName, data.rootLevel(), perkLevels));
        }

        Map<Key, PerkCreator> perkMap = Map.copyOf(perkCreatorMap);

        return new EquipmentCreator() {
            @Override
            public boolean hasEquipment(@NotNull Key equipmentKey) {
                return equipmentLevelMap.containsKey(equipmentKey);
            }

            @SuppressWarnings("unchecked")
            @NotNull
            @Override
            public <TEquipment extends Equipment> Optional<TEquipment> createEquipment(@NotNull Key equipmentKey) {
                Pair<EquipmentData, List<ElementContext>> pair = equipmentLevelMap.get(equipmentKey);
                if (pair == null) {
                    return Optional.empty();
                }

                Key equipmentType = pair.left().type();
                if (EquipmentTypes.PERK.equals(equipmentType)) {
                    return (Optional<TEquipment>)loadPerk(equipmentKey);
                }
                else if (EquipmentTypes.GUN.equals(equipmentType)) {
                    return (Optional<TEquipment>)loadGun(pair, equipmentKey);
                }
                else {
                    return Optional.empty();
                }
            }

            private Optional<Equipment> loadPerk(Key equipmentKey) {
                PerkCreator perkCreator = perkMap.get(equipmentKey);
                if (perkCreator == null) {
                    return Optional.empty();
                }

                ZombiesPlayer zombiesPlayer = equipmentModule.getZombiesPlayerSupplier().get();
                if (zombiesPlayer == null) {
                    return Optional.empty();
                }

                return Optional.of(perkCreator.forPlayer(zombiesPlayer));
            }

            private Optional<Equipment> loadGun(Pair<EquipmentData, List<ElementContext>> pair, Key equipmentKey) {
                List<ElementContext> contexts = pair.right();
                Map<Key, GunLevel> levels = new HashMap<>(contexts.size());
                Key rootLevel = pair.left().rootLevel();
                for (ElementContext context : contexts) {
                    GunLevel level = context.provide(provider, HANDLER, () -> null);
                    if (level != null) {
                        levels.put(level.data().key(), level);
                    }
                }

                if (levels.isEmpty()) {
                    return Optional.empty();
                }

                GunModel model = new GunModel(rootLevel, levels);
                Gun gun = new Gun(equipmentKey, equipmentModule.getPlayerView()::getPlayer, model);

                return Optional.of(gun);
            }
        };
    }

}
