package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigEntry;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.path.ConfigPath;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import com.github.steanky.ethylene.mapper.type.Token;
import com.github.steanky.proxima.path.Pathfinder;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.loader.DataSource;
import org.phantazm.loader.Loader;
import org.phantazm.loader.ObjectExtractor;
import org.phantazm.mob2.MobCreator;
import org.phantazm.mob2.MobData;
import org.phantazm.mob2.goal.GoalApplier;
import org.phantazm.mob2.skill.Skill;
import org.phantazm.mob2.skill.SkillComponent;
import org.phantazm.proxima.bindings.minestom.InstanceSpawner;
import org.phantazm.proxima.bindings.minestom.Pathfinding;
import org.phantazm.server.context.DataLoadingContext;
import org.phantazm.server.context.EthyleneContext;
import org.phantazm.server.context.GameContext;
import org.phantazm.zombies.mob2.ZombiesMobCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

public final class MobFeature {
    public static final Path MOBS_PATH = Path.of("./mobs");

    private static final ConfigPath PATHFINDING = ConfigPath.of("pathfinding");
    private static final ConfigPath SKILLS = ConfigPath.of("skills");
    private static final ConfigPath GOALS = ConfigPath.of("goals");

    private static final Logger LOGGER = LoggerFactory.getLogger(MobFeature.class);

    private static Loader<MobCreator> mobCreatorLoader;

    private MobFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull EthyleneContext ethyleneContext,
        @NotNull DataLoadingContext dataLoadingContext, @NotNull GameContext gameContext) {
        MappingProcessorSource processorSource = ethyleneContext.mappingProcessorSource();
        ContextManager contextManager = dataLoadingContext.contextManager();
        Pathfinder pathfinder = gameContext.pathfinder();
        Function<? super Instance, ? extends InstanceSpawner.InstanceSettings> instanceSettingsFunction =
            gameContext.instanceSettingsFunction();
        ConfigCodec codec = ethyleneContext.yamlCodec();

        ConfigProcessor<MobData> mobDataProcessor = processorSource.processorFor(Token.ofClass(MobData.class));

        mobCreatorLoader = Loader.loader(() ->
                DataSource.directory(MobFeature.MOBS_PATH, codec, "glob:**.{yml,yaml}"),
            ObjectExtractor.extractor(ConfigNode.class, (location, node) -> {
                com.github.steanky.element.core.context.ElementContext context = contextManager.makeContext(node);
                MobData data = mobDataProcessor.dataFromElement(node);

                Pathfinding.Factory pathfinding = context.provide(PATHFINDING);

                Map<EquipmentSlot, ItemStack> equipmentMap = equipmentMap(data.equipment(), processorSource);
                Object2FloatMap<String> attributeMap = attributeMap(data.attributes());
                List<Pair<Attribute, AttributeModifier>> attributeModifiers = attributeModifiers(data.attributeModifiers());

                List<SkillComponent> skillComponents = data.skills().isEmpty() ? List.of() : context.provideCollection(SKILLS);
                List<GoalApplier> goals = data.goals().isEmpty() ? List.of() : context.provideCollection(GOALS);

                List<Skill> skills = new ArrayList<>(skillComponents.size());
                for (SkillComponent component : skillComponents) {
                    skills.add(component.get());
                }


                return List.of(ObjectExtractor.entry(data.key(), (MobCreator) new ZombiesMobCreator(data,
                    pathfinding, skills, goals, pathfinder, instanceSettingsFunction, equipmentMap,
                    attributeMap, attributeModifiers)));
            })).accepting(mobs -> {
            LOGGER.info("Loaded {} mob file(s)", mobs.size());
        });

        mobCreatorLoader.loadUnchecked();
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

    private static Object2FloatMap<String> attributeMap(ConfigNode node) throws ConfigProcessException {
        Object2FloatMap<String> map = new Object2FloatOpenHashMap<>(node.size());
        for (ConfigEntry entry : node.entryCollection()) {
            String attribute = entry.getKey();
            float value = ConfigProcessor.FLOAT.dataFromElement(entry.getValue());
            map.put(attribute, value);
        }

        return map;
    }

    private static List<Pair<Attribute, AttributeModifier>> attributeModifiers(ConfigNode elementNode) {
        if (elementNode.isEmpty()) {
            return List.of();
        }

        List<Pair<Attribute, AttributeModifier>> modifiers = new ArrayList<>(elementNode.size());
        for (Map.Entry<String, ConfigElement> entry : elementNode.entrySet()) {
            String key = entry.getKey();
            ConfigElement value = entry.getValue();
            if (!value.isNumber()) {
                continue;
            }

            float valueNumber = value.asNumber().floatValue();

            int lastDot = -1;
            for (int i = key.length() - 1; i >= 0; i--) {
                char c = key.charAt(i);

                if (c == '.') {
                    lastDot = i;
                    break;
                }
            }

            if (lastDot == -1) {
                continue;
            }

            AttributeOperation operation = switch (key.substring(lastDot + 1).toLowerCase(Locale.ROOT)) {
                case "addition" -> AttributeOperation.ADDITION;
                case "multiply_total" -> AttributeOperation.MULTIPLY_TOTAL;
                case "multiply_base" -> AttributeOperation.MULTIPLY_BASE;
                default -> null;
            };

            if (operation == null) {
                continue;
            }

            String attributeString = key.substring(0, lastDot);
            Attribute attribute = Attribute.fromKey(attributeString);
            if (attribute == null) {
                continue;
            }

            UUID uuid = UUID.randomUUID();
            String uuidStr = uuid.toString();
            modifiers.add(Pair.of(attribute, new AttributeModifier(uuid, uuidStr, valueNumber, operation)));
        }

        return modifiers.isEmpty() ? List.of() : modifiers;
    }

    @SuppressWarnings("unused")
    public static @NotNull Loader<MobCreator> mobLoader() {
        return FeatureUtils.check(mobCreatorLoader);
    }

    public static void reload() throws IOException {
        FeatureUtils.check(mobCreatorLoader).load();
    }
}
