package com.github.phantazmnetwork.mob;

import com.github.phantazmnetwork.mob.skill.Skill;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.MinestomDescriptor;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BasicMobModel<TDescriptor extends MinestomDescriptor> extends MobModel<TDescriptor, NeuralEntity> {

    public BasicMobModel(@NotNull TDescriptor descriptor, @NotNull Map<String, Skill> skills,
                         @Nullable Component displayName, @NotNull Map<EquipmentSlot, ItemStack> equipment) {
        super(descriptor, NeuralEntity::new, skills, displayName, equipment);
    }

}
