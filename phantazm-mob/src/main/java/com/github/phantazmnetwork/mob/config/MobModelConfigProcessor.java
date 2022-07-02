package com.github.phantazmnetwork.mob.config;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.goal.Goal;
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
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A {@link ConfigProcessor} for {@link MobModel}s.
 */
public class MobModelConfigProcessor implements ConfigProcessor<MobModel> {

    private static final ConfigProcessor<EquipmentSlot> EQUIPMENT_SLOT_PROCESSOR = ConfigProcessor.enumProcessor(EquipmentSlot.class);

    private static final ConfigProcessor<Key> KEY_PROCESSOR = AdventureConfigProcessors.key();

    private static final ConfigProcessor<Component> COMPONENT_PROCESSOR = AdventureConfigProcessors.component();

    private final ConfigProcessor<MinestomDescriptor> descriptorProcessor;

    private final ConfigProcessor<Goal> goalProcessor;

    private final ConfigProcessor<Skill> skillProcessor;

    private final ConfigProcessor<ItemStack> itemStackProcessor;

    /**
     * Creates a new {@link MobModelConfigProcessor}.
     * @param descriptorProcessor A {@link ConfigProcessor} for {@link MinestomDescriptor}s
     * @param goalProcessor A {@link ConfigProcessor} for {@link Goal}s
     * @param skillProcessor A {@link ConfigProcessor} for {@link Skill}s
     * @param itemStackProcessor A {@link ConfigProcessor} for {@link ItemStack}s
     */
    public MobModelConfigProcessor(@NotNull ConfigProcessor<MinestomDescriptor> descriptorProcessor,
                                   @NotNull ConfigProcessor<Goal> goalProcessor,
                                   @NotNull ConfigProcessor<Skill> skillProcessor,
                                   @NotNull ConfigProcessor<ItemStack> itemStackProcessor) {
        this.descriptorProcessor = Objects.requireNonNull(descriptorProcessor, "descriptorProcessor");
        this.goalProcessor = Objects.requireNonNull(goalProcessor, "goalProcessor");
        this.skillProcessor = Objects.requireNonNull(skillProcessor, "skillProcessor");
        this.itemStackProcessor = Objects.requireNonNull(itemStackProcessor, "itemStackProcessor");
    }

    @Override
    public @NotNull MobModel dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        Key key = KEY_PROCESSOR.dataFromElement(element.getElementOrThrow("key"));

        MinestomDescriptor descriptor = descriptorProcessor.dataFromElement(element.getElement("descriptor"));

        ConfigList goalGroupsList = element.getListOrThrow("goalGroups");
        Collection<Collection<Goal>> goalGroups = new ArrayList<>(goalGroupsList.size());
        for (ConfigElement goalGroupElement : goalGroupsList) {
            if (!goalGroupElement.isList()) {
                throw new ConfigProcessException("goal groups are not a list");
            }

            ConfigList goalsList = goalGroupElement.asList();
            Collection<Goal> goals = new ArrayList<>(goalsList.size());
            for (ConfigElement goalElement : goalsList) {
                goals.add(goalProcessor.dataFromElement(goalElement));
            }

            goalGroups.add(List.copyOf(goals));
        }

        ConfigList triggersList = element.getListOrThrow("triggers");
        Map<Key, Collection<Skill>> triggers = new HashMap<>(triggersList.size());
        for (ConfigElement triggerElement : triggersList) {
            Key triggerKey = KEY_PROCESSOR.dataFromElement(triggerElement.getElementOrThrow("key"));

            ConfigList skillsList = triggerElement.getListOrThrow("skills");
            Collection<Skill> skills = new ArrayList<>(skillsList.size());
            for (ConfigElement skillElement : skillsList) {
                Skill skillInstance = skillProcessor.dataFromElement(skillElement);
                skills.add(skillInstance);
            }

            triggers.put(triggerKey, List.copyOf(skills));
        }

        Component displayName;
        ConfigElement displayNameElement = element.getElementOrDefault(() -> new ConfigPrimitive(null), "displayName");
        if (element.isNull()) {
            displayName = null;
        }
        else {
            displayName = COMPONENT_PROCESSOR.dataFromElement(displayNameElement);
        }

        ConfigNode equipmentNode = element.getNodeOrThrow("equipment");
        Map<EquipmentSlot, ItemStack> equipment = new HashMap<>(equipmentNode.size());
        for (Map.Entry<String, ConfigElement> entry : equipmentNode.entrySet()) {
            EquipmentSlot equipmentSlot = EQUIPMENT_SLOT_PROCESSOR.dataFromElement(new ConfigPrimitive(entry.getKey().toUpperCase()));
            equipment.put(equipmentSlot, itemStackProcessor.dataFromElement(entry.getValue()));
        }

        float maxHealth = element.getNumberOrThrow("maxHealth").floatValue();

        float speed = element.getNumberOrThrow("speed").floatValue();

        return new MobModel(key, descriptor, goalGroups, triggers, displayName, equipment, maxHealth, speed);
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull MobModel model) throws ConfigProcessException {
        ConfigElement key = KEY_PROCESSOR.elementFromData(model.key());

        ConfigElement descriptor = descriptorProcessor.elementFromData(model.getDescriptor());

        ConfigList goalGroupsList = new ArrayConfigList(model.getGoalGroups().size());
        for (Collection<Goal> group : model.getGoalGroups()) {
            ConfigList goalGroupList = new ArrayConfigList(group.size());
            for (Goal goal : group) {
                goalGroupList.add(goalProcessor.elementFromData(goal));
            }

            goalGroupsList.add(goalGroupList);
        }

        ConfigList triggers = new ArrayConfigList(model.getTriggers().size());
        for (Map.Entry<Key, Collection<Skill>> trigger : model.getTriggers().entrySet()) {
            ConfigList skillList = new ArrayConfigList(trigger.getValue().size());
            for (Skill skill : trigger.getValue()) {
                skillList.add(skillProcessor.elementFromData(skill));
            }

            ConfigNode node = new LinkedConfigNode(2);
            node.put("key", KEY_PROCESSOR.elementFromData(trigger.getKey()));
            node.put("skills", skillList);
            triggers.add(node);
        }

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
                throw new ConfigProcessException("equipment slot processor did not create a string");
            }
            equipmentNode.put(slotElement.asString(), itemStackProcessor.elementFromData(entry.getValue()));
        }

        ConfigNode element = new LinkedConfigNode(7);
        element.put("key", key);
        element.put("descriptor", descriptor);
        element.put("goalGroups", goalGroupsList);
        element.put("triggers", triggers);
        element.put("displayName", displayNameElement);
        element.put("equipment", equipmentNode);
        element.putNumber("maxHealth", model.getMaxHealth());
        element.putNumber("speed", model.getSpeed());

        return element;
    }
}
