package com.github.phantazmnetwork.mob.config;

import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.goal.GoalCreator;
import com.github.phantazmnetwork.mob.skill.Skill;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.MinestomDescriptor;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ArrayConfigList;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MobModelConfigProcessor implements ConfigProcessor<MobModel> {

    private final static Map<String, EquipmentSlot> EQUIPMENT_SLOT_MAP = new HashMap<>(EquipmentSlot.values().length);

    private final ConfigProcessor<MinestomDescriptor> descriptorProcessor;

    private final ConfigProcessor<GoalCreator> goalCreatorConfigProcessor;

    private final ConfigProcessor<ItemStack> itemStackConfigProcessor;

    private final MiniMessage miniMessage;

    static {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            EQUIPMENT_SLOT_MAP.put(equipmentSlot.name().toUpperCase(), equipmentSlot);
        }
    }

    public MobModelConfigProcessor(@NotNull ConfigProcessor<MinestomDescriptor> descriptorProcessor,
                                   @NotNull ConfigProcessor<GoalCreator> goalCreatorConfigProcessor,
                                   @NotNull ConfigProcessor<ItemStack> itemStackConfigProcessor,
                                   @NotNull MiniMessage miniMessage) {
        this.descriptorProcessor = Objects.requireNonNull(descriptorProcessor, "descriptorProcessor");
        this.goalCreatorConfigProcessor = Objects.requireNonNull(goalCreatorConfigProcessor,
                "goalCreatorConfigProcessor");
        this.itemStackConfigProcessor = Objects.requireNonNull(itemStackConfigProcessor,
                "itemStackConfigProcessor");
        this.miniMessage = Objects.requireNonNull(miniMessage, "miniMessage");
    }

    @Override
    public @NotNull MobModel dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        MinestomDescriptor descriptor = descriptorProcessor.dataFromElement(element.getElement("descriptor"));

        ConfigList goalCreatorsGroupsList = element.getListOrThrow("goalCreators");
        Collection<Iterable<GoalCreator>> goalCreatorsGroups = new ArrayList<>(goalCreatorsGroupsList.size());
        for (ConfigElement goalCreatorsElement : goalCreatorsGroupsList) {
            if (!goalCreatorsElement.isList()) {
                throw new ConfigProcessException("goal creators are not a list");
            }

            ConfigList goalCreatorsList = goalCreatorsElement.asList();
            Collection<GoalCreator> goalCreators = new ArrayList<>(goalCreatorsList.size());
            for (ConfigElement goalCreatorElement : goalCreatorsList) {
                goalCreators.add(goalCreatorConfigProcessor.dataFromElement(goalCreatorElement));
            }

            goalCreatorsGroups.add(goalCreators);
        }

        String displayNameString = element.getStringOrDefault((String) null, "displayName");
        Component displayName = displayNameString == null ? null : miniMessage.deserialize(displayNameString);

        ConfigNode equipmentNode = element.getNodeOrThrow("equipment");
        Map<EquipmentSlot, ItemStack> equipment = new HashMap<>(equipmentNode.size());
        for (Map.Entry<String, ConfigElement> entry : equipmentNode.entrySet()) {
            EquipmentSlot equipmentSlot = EQUIPMENT_SLOT_MAP.get(entry.getKey().toUpperCase());
            if (equipmentSlot == null) {
                throw new ConfigProcessException("unknown equipment slot");
            }

            if (!entry.getValue().isString()) {
                throw new ConfigProcessException("equipment value is not string");
            }

            equipment.put(equipmentSlot, itemStackConfigProcessor.dataFromElement(entry.getValue()));
        }

        float maxHealth = element.getNumberOrThrow("maxHealth").floatValue();

        return new MobModel(descriptor, goalCreatorsGroups, displayName, equipment, maxHealth);
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull MobModel model) throws ConfigProcessException {
        ConfigElement descriptor = descriptorProcessor.elementFromData(model.getDescriptor());

        ConfigList goalCreatorsGroups = new ArrayConfigList();
        for (Iterable<GoalCreator> creators : model.getGoalCreatorsGroups()) {
            ConfigList goalCreatorsList = new ArrayConfigList();
            for (GoalCreator creator : creators) {
                goalCreatorsList.add(goalCreatorConfigProcessor.elementFromData(creator));
            }

            goalCreatorsGroups.add(goalCreatorsList);
        }

        ConfigElement displayName = model.getDisplayName()
                .map(miniMessage::serialize)
                .map(ConfigPrimitive::new)
                .orElseGet(() -> new ConfigPrimitive(null));

        ConfigNode equipmentNode = new LinkedConfigNode();
        for (Map.Entry<EquipmentSlot, ItemStack> entry : model.getEquipment().entrySet()) {
            equipmentNode.put(entry.getKey().name().toUpperCase(), itemStackConfigProcessor.elementFromData(entry.getValue()));
        }

        ConfigElement maxHealth = new ConfigPrimitive(model.getMaxHealth());

        ConfigNode element = new LinkedConfigNode();
        element.put("descriptor", descriptor);
        element.put("goalGroupCreators", goalCreatorsGroups);
        element.put("displayName", displayName);
        element.put("equipment", equipmentNode);
        element.put("maxHealth", maxHealth);

        return element;
    }
}
