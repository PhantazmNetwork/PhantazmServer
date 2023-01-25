package org.phantazm.mob.config;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigEntry;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ConfigProcessors;
import org.phantazm.core.ElementUtils;
import org.phantazm.mob.MobModel;
import org.phantazm.proxima.bindings.minestom.GroundPathfindingFactory;
import org.phantazm.proxima.bindings.minestom.Pathfinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A {@link ConfigProcessor} for {@link MobModel}s.
 */
public class MobModelConfigProcessor implements ConfigProcessor<MobModel> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MobModelConfigProcessor.class);
    private static final Consumer<? super ElementException> HANDLER = ElementUtils.logging(LOGGER, "pathfinding");

    private static final ConfigProcessor<EquipmentSlot> EQUIPMENT_SLOT_PROCESSOR =
            ConfigProcessor.enumProcessor(EquipmentSlot.class);

    private static final ConfigProcessor<Key> KEY_PROCESSOR = ConfigProcessors.key();

    private static final ConfigProcessor<Component> COMPONENT_PROCESSOR = ConfigProcessors.component();

    private static final ConfigProcessor<Object2FloatMap<String>> ATTRIBUTE_MAP_PROCESSOR =
            ConfigProcessor.FLOAT.mapProcessor(Object2FloatOpenHashMap::new);

    private final ContextManager contextManager;

    private final ConfigProcessor<ItemStack> itemStackProcessor;

    /**
     * Creates a new {@link MobModelConfigProcessor}.
     *
     * @param contextManager     A {@link ContextManager} used to load {@link Pathfinding.Factory} objects
     * @param itemStackProcessor A {@link ConfigProcessor} for {@link ItemStack}s
     */
    public MobModelConfigProcessor(@NotNull ContextManager contextManager,
            @NotNull ConfigProcessor<ItemStack> itemStackProcessor) {
        this.contextManager = Objects.requireNonNull(contextManager, "contextManager");
        this.itemStackProcessor = Objects.requireNonNull(itemStackProcessor, "itemStackProcessor");
    }

    @Override
    public @NotNull MobModel dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        if (!element.isNode()) {
            throw new ConfigProcessException("element must be a node");
        }
        ConfigNode node = element.asNode();

        Key key = KEY_PROCESSOR.dataFromElement(element.getElementOrThrow("key"));
        EntityType entityType = EntityType.fromNamespaceId(
                NamespaceID.from(KEY_PROCESSOR.dataFromElement(element.getElementOrThrow("entityType"))));

        ElementContext context = contextManager.makeContext(element.getNodeOrThrow("settings"));

        Pathfinding.Factory factory;
        try {
            factory = context.provide(DependencyProvider.EMPTY);
        }
        catch (ElementException e) {
            HANDLER.accept(e);
            factory = new GroundPathfindingFactory(new GroundPathfindingFactory.Data(1, 4, 0.5F));
        }

        ConfigNode metaNode = element.getNodeOrThrow("meta");

        Component displayName;
        ConfigElement displayNameElement = element.getElementOrDefault(() -> ConfigPrimitive.NULL, "displayName");
        if (displayNameElement.isNull()) {
            displayName = null;
        }
        else {
            displayName = COMPONENT_PROCESSOR.dataFromElement(displayNameElement);
        }

        ConfigNode equipmentNode = element.getNodeOrThrow("equipment");
        Map<EquipmentSlot, ItemStack> equipment = new HashMap<>(equipmentNode.size());
        for (Map.Entry<String, ConfigElement> entry : equipmentNode.entrySet()) {
            EquipmentSlot equipmentSlot =
                    EQUIPMENT_SLOT_PROCESSOR.dataFromElement(ConfigPrimitive.of(entry.getKey().toUpperCase()));
            equipment.put(equipmentSlot, itemStackProcessor.dataFromElement(entry.getValue()));
        }

        Object2FloatMap<String> attributes =
                ATTRIBUTE_MAP_PROCESSOR.dataFromElement(element.getElementOrThrow("attributes"));

        return new MobModel(key, entityType, factory, node, metaNode, displayName, equipment, attributes);
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull MobModel model) throws ConfigProcessException {
        ConfigElement key = KEY_PROCESSOR.elementFromData(model.key());

        Optional<Component> displayName = model.getDisplayName();
        ConfigElement displayNameElement;
        if (displayName.isPresent()) {
            displayNameElement = COMPONENT_PROCESSOR.elementFromData(displayName.get());
        }
        else {
            displayNameElement = ConfigPrimitive.NULL;
        }

        ConfigNode equipmentNode = new LinkedConfigNode(model.getEquipment().entrySet().size());
        for (Map.Entry<EquipmentSlot, ItemStack> entry : model.getEquipment().entrySet()) {
            ConfigElement slotElement = EQUIPMENT_SLOT_PROCESSOR.elementFromData(entry.getKey());
            if (!slotElement.isString()) {
                throw new ConfigProcessException("Equipment slot processor did not create a string");
            }
            equipmentNode.put(slotElement.asString(), itemStackProcessor.elementFromData(entry.getValue()));
        }

        ConfigElement attributes = ATTRIBUTE_MAP_PROCESSOR.elementFromData(model.getAttributes());

        ConfigNode element = new LinkedConfigNode(7);
        element.put("key", key);
        element.put("settings", ConfigNode.of());
        element.put("meta", model.getMetaNode());
        element.put("displayName", displayNameElement);
        element.put("equipment", equipmentNode);
        element.put("attributes", attributes);
        for (ConfigEntry entry : model.getNode().entryCollection()) {
            if (!element.containsKey(entry.getKey())) {
                element.put(entry.getKey(), entry.getValue());
            }
        }

        return element;
    }
}
