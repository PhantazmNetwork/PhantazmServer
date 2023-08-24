package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.path.ElementPath;
import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.bridge.Configuration;
import com.github.steanky.ethylene.core.collection.ConfigEntry;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import com.github.steanky.ethylene.mapper.type.Token;
import com.github.steanky.proxima.path.Pathfinder;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.FileUtils;
import org.phantazm.mob2.MobCreator;
import org.phantazm.mob2.MobData;
import org.phantazm.mob2.goal.GoalApplier;
import org.phantazm.mob2.skill.SkillComponent;
import org.phantazm.proxima.bindings.minestom.InstanceSpawner;
import org.phantazm.proxima.bindings.minestom.Pathfinding;
import org.phantazm.zombies.mob2.ZombiesMobCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public final class MobFeature {
    public static final Path MOBS_PATH = Path.of("./mobs");

    private static final ElementPath PATHFINDING = ElementPath.of("/pathfinding");
    private static final ElementPath SKILLS = ElementPath.of("/skills");
    private static final ElementPath GOALS = ElementPath.of("/goals");

    private static final Logger LOGGER = LoggerFactory.getLogger(MobFeature.class);

    private static Map<Key, MobCreator> creators;

    private MobFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull ConfigCodec codec, @NotNull MappingProcessorSource processorSource,
        @NotNull ContextManager contextManager, @NotNull Pathfinder pathfinder,
        @NotNull Function<? super @NotNull Instance, ? extends InstanceSpawner.InstanceSettings> instanceSettingsFunction) {
        ConfigProcessor<MobData> mobDataProcessor = processorSource.processorFor(Token.ofClass(MobData.class));
        Map<Key, MobCreator> loadedCreators = new HashMap<>();
        try {
            FileUtils.createDirectories(MobFeature.MOBS_PATH);

            try (Stream<Path> paths = Files.walk(MobFeature.MOBS_PATH)) {
                String ending = codec.getPreferredExtension();
                PathMatcher matcher = MobFeature.MOBS_PATH.getFileSystem().getPathMatcher("glob:**" + ending);
                paths.forEach(path -> {
                    if (matcher.matches(path) && Files.isRegularFile(path)) {
                        try {
                            ConfigElement element = Configuration.read(path, codec);
                            if (!element.isNode()) {
                                LOGGER.warn("Non-node mob file " + path);
                                return;
                            }

                            ConfigNode node = element.asNode();
                            ElementContext context = contextManager.makeContext(node);

                            MobData data = mobDataProcessor.dataFromElement(node);

                            if (loadedCreators.containsKey(data.key())) {
                                LOGGER.warn("Duplicate key ({}), skipping...", data.key());
                                return;
                            }

                            Pathfinding.Factory pathfinding = context.provide(PATHFINDING);

                            Map<EquipmentSlot, ItemStack> equipmentMap = equipmentMap(data.equipment(), processorSource);
                            Object2FloatMap<String> attributeMap = attributeMap(data.attributes(), processorSource);

                            List<SkillComponent> skills = data.skills().isEmpty() ? List.of() : context.provideCollection(SKILLS);
                            List<GoalApplier> goals = data.goals().isEmpty() ? List.of() : context.provideCollection(GOALS);

                            loadedCreators.put(data.key(), new ZombiesMobCreator(data, pathfinding, skills, goals,
                                pathfinder, instanceSettingsFunction, equipmentMap, attributeMap));
                        } catch (IOException e) {
                            LOGGER.warn("Could not load mob file", e);
                        }
                    }
                });
            } catch (IOException e) {
                LOGGER.warn("Could not list files in mob directory", e);
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to create directory {}", MobFeature.MOBS_PATH);
        }
        creators = Map.copyOf(loadedCreators);
        LOGGER.info("Loaded {} mob files.", creators.size());
    }

    @SuppressWarnings("unchecked")
    private static Map<EquipmentSlot, ItemStack> equipmentMap(ConfigNode node, MappingProcessorSource processorSource) throws ConfigProcessException {
        ConfigProcessor<EquipmentSlot> equipmentSlotProcessor = processorSource.processorFor(Token.ofClass(EquipmentSlot.class));
        ConfigProcessor<ItemStack> itemStackProcessor = processorSource.processorFor(Token.ofClass(ItemStack.class));

        Map.Entry<EquipmentSlot, ItemStack>[] entries = new Map.Entry[node.size()];
        int i = 0;
        for (ConfigEntry entry : node.entryCollection()) {
            EquipmentSlot slot = equipmentSlotProcessor.dataFromElement(ConfigPrimitive.of(entry.getKey()));
            ItemStack item = itemStackProcessor.dataFromElement(entry.getValue());
            entries[i++] = Map.entry(slot, item);
        }

        return Map.ofEntries(entries);
    }

    private static Object2FloatMap<String> attributeMap(ConfigNode node, MappingProcessorSource processorSource) throws ConfigProcessException {
        Object2FloatMap<String> map = new Object2FloatOpenHashMap<>(node.size());
        for (ConfigEntry entry : node.entryCollection()) {
            String attribute = entry.getKey();
            float value = ConfigProcessor.FLOAT.dataFromElement(entry.getValue());
            map.put(attribute, value);
        }

        return map;
    }

    @SuppressWarnings("unused")
    public static @NotNull @Unmodifiable Map<Key, MobCreator> getMobCreators() {
        return FeatureUtils.check(creators);
    }
}
