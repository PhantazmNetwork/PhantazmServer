package com.github.phantazmnetwork.mob.config;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.MinestomDescriptor;
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
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A {@link ConfigProcessor} for {@link MobModel}s.
 */
public class MobModelConfigProcessor implements ConfigProcessor<MobModel> {

    private static final ConfigProcessor<EquipmentSlot> EQUIPMENT_SLOT_PROCESSOR =
            ConfigProcessor.enumProcessor(EquipmentSlot.class);

    private static final ConfigProcessor<Key> KEY_PROCESSOR = ConfigProcessors.key();

    private static final ConfigProcessor<Component> COMPONENT_PROCESSOR = ConfigProcessors.component();

    private static final ConfigProcessor<Object2FloatMap<String>> ATTRIBUTE_MAP_PROCESSOR =
            ConfigProcessor.FLOAT.mapProcessor(Object2FloatOpenHashMap::new);

    private final ConfigProcessor<MinestomDescriptor> descriptorProcessor;

    private final ConfigProcessor<ItemStack> itemStackProcessor;

    /**
     * Creates a new {@link MobModelConfigProcessor}.
     *
     * @param descriptorProcessor A {@link ConfigProcessor} for {@link MinestomDescriptor}s
     * @param itemStackProcessor  A {@link ConfigProcessor} for {@link ItemStack}s
     */
    public MobModelConfigProcessor(@NotNull ConfigProcessor<MinestomDescriptor> descriptorProcessor,
            @NotNull ConfigProcessor<ItemStack> itemStackProcessor) {
        this.descriptorProcessor = Objects.requireNonNull(descriptorProcessor, "descriptorProcessor");
        this.itemStackProcessor = Objects.requireNonNull(itemStackProcessor, "itemStackProcessor");
    }

    @Override
    public @NotNull MobModel dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        if (!element.isNode()) {
            throw new ConfigProcessException("element must be a node");
        }
        ConfigNode node = element.asNode();

        Key key = KEY_PROCESSOR.dataFromElement(element.getElementOrThrow("key"));

        MinestomDescriptor descriptor = descriptorProcessor.dataFromElement(element.getElement("descriptor"));

        ConfigNode metaNode = element.getNodeOrThrow("meta");

        Component displayName;
        ConfigElement displayNameElement = element.getElementOrDefault(() -> new ConfigPrimitive(null), "displayName");
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
                    EQUIPMENT_SLOT_PROCESSOR.dataFromElement(new ConfigPrimitive(entry.getKey().toUpperCase()));
            equipment.put(equipmentSlot, itemStackProcessor.dataFromElement(entry.getValue()));
        }

        Object2FloatMap<String> attributes =
                ATTRIBUTE_MAP_PROCESSOR.dataFromElement(element.getElementOrThrow("attributes"));

        return new MobModel(key, descriptor, node, metaNode, displayName, equipment, attributes);
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull MobModel model) throws ConfigProcessException {
        ConfigElement key = KEY_PROCESSOR.elementFromData(model.key());

        ConfigElement descriptor = descriptorProcessor.elementFromData(model.getDescriptor());

        Optional<Component> displayName = model.getDisplayName();
        ConfigElement displayNameElement;
        if (displayName.isPresent()) {
            displayNameElement = COMPONENT_PROCESSOR.elementFromData(displayName.get());
        }
        else {
            displayNameElement = new ConfigPrimitive(null);
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
        element.put("descriptor", descriptor);
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
