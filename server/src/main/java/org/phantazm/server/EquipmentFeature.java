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
import org.phantazm.commons.FileUtils;
import org.phantazm.core.ElementUtils;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.core.equipment.EquipmentCreator;
import org.phantazm.zombies.equipment.EquipmentData;
import org.phantazm.zombies.equipment.EquipmentTypes;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.GunLevel;
import org.phantazm.zombies.equipment.gun.GunModel;
import org.phantazm.zombies.equipment.gun.ZombiesEquipmentModule;
import org.phantazm.zombies.equipment.gun.event.GunShootEvent;
import org.phantazm.zombies.equipment.perk.BasicPerkCreator;
import org.phantazm.zombies.equipment.perk.PerkCreator;
import org.phantazm.zombies.equipment.perk.level.PerkLevelCreator;
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
        EquipmentFeature.keyParser = Objects.requireNonNull(keyParser);

        String ending;
        if (codec.getPreferredExtensions().isEmpty()) {
            ending = "";
        } else {
            ending = "." + codec.getPreferredExtension();
        }

        PathMatcher matcher = EQUIPMENT_PATH.getFileSystem().getPathMatcher("glob:**" + ending);

        Path guns = EQUIPMENT_PATH.resolve("guns");
        Path perks = EQUIPMENT_PATH.resolve("perks");
        try {
            FileUtils.createDirectories(guns);
            FileUtils.createDirectories(perks);
        } catch (IOException e) {
            LOGGER.warn("Failed to create a necessary equipment directory.", e);
            return;
        }

        List<Path> equipmentDirectories = List.of(guns, perks);
        Map<Key, Pair<EquipmentData, List<ElementContext>>> equipmentLevelMap = new HashMap<>();

        for (Path equipmentDirectory : equipmentDirectories) {
            try (Stream<Path> gunDirectories = Files.list(equipmentDirectory)) {
                for (Path gunDirectory : (Iterable<? extends Path>) gunDirectories::iterator) {
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
                    } catch (ConfigProcessException e) {
                        LOGGER.warn("Failed to read equipment settings file at {}.", infoPath, e);
                        continue;
                    }

                    List<ElementContext> levelData = new ArrayList<>();
                    Path levelsPath = gunDirectory.resolve("levels");
                    try (Stream<Path> levelDirectories = Files.list(levelsPath)) {
                        for (Path levelFile : (Iterable<? extends Path>) levelDirectories::iterator) {
                            if (!(Files.isRegularFile(levelFile) && matcher.matches(levelFile))) {
                                continue;
                            }

                            try {
                                ConfigNode node = Configuration.read(levelFile, codec, ConfigProcessor.CONFIG_NODE);
                                levelData.add(contextManager.makeContext(node));
                            } catch (IOException e) {
                                LOGGER.warn("Failed to read level file at {}.", levelFile, e);
                            }
                        }
                    } catch (IOException e) {
                        LOGGER.warn("Failed to list levels directory at {}.", levelsPath, e);
                        continue;
                    }

                    equipmentLevelMap.put(equipmentData.name(), Pair.of(equipmentData, levelData));
                }
            } catch (IOException e) {
                LOGGER.warn("Failed to list equipment directory at {}", guns, e);
            }
        }

        EquipmentFeature.equipmentLevelMap = Map.copyOf(equipmentLevelMap);
        LOGGER.info("Loaded {} equipment.", equipmentLevelMap.size());
    }

    public static @NotNull EquipmentCreator createEquipmentCreator(@NotNull ZombiesEquipmentModule equipmentModule) {
        Objects.requireNonNull(equipmentModule);
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
                PerkLevelCreator perkLevelCreator = context.provide(provider, HANDLER.andThen(exception -> {
                    LOGGER.warn("Erroring perk: " + data.name());
                }), () -> null);
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
                    return (Optional<TEquipment>) loadPerk(equipmentKey);
                } else if (EquipmentTypes.GUN.equals(equipmentType)) {
                    return (Optional<TEquipment>) loadGun(pair, equipmentKey);
                }

                return Optional.empty();
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
                equipmentModule.getEventNode().addListener(GunShootEvent.class, event -> {
                    if (event.gun() != gun) {
                        return;
                    }

                    equipmentModule.getMapStats().setShots(equipmentModule.getMapStats().getShots() + 1);

                    if (!event.shot().headshotTargets().isEmpty()) {
                        equipmentModule.getMapStats()
                            .setHeadshotHits(equipmentModule.getMapStats().getHeadshotHits() + 1);
                    } else if (!event.shot().regularTargets().isEmpty()) {
                        equipmentModule.getMapStats()
                            .setRegularHits(equipmentModule.getMapStats().getRegularHits() + 1);
                    }
                });

                return Optional.of(gun);
            }
        };
    }

}
