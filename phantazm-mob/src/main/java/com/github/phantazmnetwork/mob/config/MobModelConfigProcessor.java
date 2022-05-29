package com.github.phantazmnetwork.mob.config;

import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.goal.GoalGroupCreator;
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

    private final ConfigProcessor<Skill> skillConfigProcessor;

    private final ConfigProcessor<GoalGroupCreator> goalGroupCreatorConfigProcessor;

    private final ConfigProcessor<ItemStack> itemStackConfigProcessor;

    private final MiniMessage miniMessage;

    static {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            EQUIPMENT_SLOT_MAP.put(equipmentSlot.name().toUpperCase(), equipmentSlot);
        }
    }

    public MobModelConfigProcessor(@NotNull ConfigProcessor<MinestomDescriptor> descriptorProcessor,
                                   @NotNull ConfigProcessor<Skill> skillConfigProcessor,
                                   @NotNull ConfigProcessor<GoalGroupCreator> goalGroupCreatorConfigProcessor,
                                   @NotNull ConfigProcessor<ItemStack> itemStackConfigProcessor,
                                   @NotNull MiniMessage miniMessage) {
        this.descriptorProcessor = Objects.requireNonNull(descriptorProcessor, "descriptorProcessor");
        this.skillConfigProcessor = Objects.requireNonNull(skillConfigProcessor, "skillConfigProcessor");
        this.goalGroupCreatorConfigProcessor = Objects.requireNonNull(goalGroupCreatorConfigProcessor,
                "goalGroupCreatorConfigProcessor");
        this.itemStackConfigProcessor = Objects.requireNonNull(itemStackConfigProcessor,
                "itemStackConfigProcessor");
        this.miniMessage = Objects.requireNonNull(miniMessage, "miniMessage");
    }

    @Override
    public @NotNull MobModel dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        MinestomDescriptor descriptor = descriptorProcessor.dataFromElement(element.getElement("descriptor"));

        ConfigNode skillsNode = element.getNodeOrThrow("skills");
        Map<String, Skill> skills = new HashMap<>(skillsNode.size());
        for (Map.Entry<String, ConfigElement> entry : skillsNode.entrySet()) {
            skills.put(entry.getKey(), skillConfigProcessor.dataFromElement(entry.getValue()));
        }

        ConfigList goalGroupCreatorsList = element.getListOrThrow("goalGroupCreators");
        Collection<GoalGroupCreator> goalGroupCreators = new ArrayList<>(goalGroupCreatorsList.size());
        for (ConfigElement goalGroupCreatorElement : goalGroupCreatorsList) {
            goalGroupCreators.add(goalGroupCreatorConfigProcessor.dataFromElement(goalGroupCreatorElement));
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

        return new MobModel(descriptor, skills, goalGroupCreators, displayName, equipment);
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull MobModel model) throws ConfigProcessException {
        ConfigElement descriptor = descriptorProcessor.elementFromData(model.getDescriptor());

        ConfigNode skillsNode = new LinkedConfigNode();
        for (Map.Entry<String, Skill> entry : model.getSkills().entrySet()) {
            skillsNode.put(entry.getKey(), skillConfigProcessor.elementFromData(entry.getValue()));
        }

        ConfigList goalGroupCreators = new ArrayConfigList();
        for (GoalGroupCreator creator : model.getGoalGroupCreators()) {
            goalGroupCreators.add(goalGroupCreatorConfigProcessor.elementFromData(creator));
        }

        ConfigElement displayName = model.getDisplayName()
                .map(miniMessage::serialize)
                .map(ConfigPrimitive::new)
                .orElseGet(() -> new ConfigPrimitive(null));

        ConfigNode equipmentNode = new LinkedConfigNode();
        for (Map.Entry<EquipmentSlot, ItemStack> entry : model.getEquipment().entrySet()) {
            equipmentNode.put(entry.getKey().name().toUpperCase(), itemStackConfigProcessor.elementFromData(entry.getValue()));
        }

        ConfigNode element = new LinkedConfigNode();
        element.put("descriptor", descriptor);
        element.put("skills", skillsNode);
        element.put("goalGroupCreators", goalGroupCreators);
        element.put("displayName", displayName);
        element.put("equipment", equipmentNode);

        return element;
}
}
