package com.github.phantazmnetwork.mob;

import com.github.phantazmnetwork.mob.goal.Goal;
import com.github.phantazmnetwork.mob.skill.Skill;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.MinestomDescriptor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public class MobModel implements Keyed {

    private final Key key;

    private final MinestomDescriptor descriptor;

    private final Collection<Collection<Goal>> goalCreatorsGroups;

    private final Map<Key, Collection<Skill>> triggers;

    private final Component displayName;

    private final Map<EquipmentSlot, ItemStack> equipment;

    private final float maxHealth;

    public MobModel(@NotNull Key key, @NotNull MinestomDescriptor descriptor,
                    @NotNull Collection<Collection<Goal>> goalCreatorsGroups,
                    @NotNull Map<Key, Collection<Skill>> triggers, @Nullable Component displayName,
                    @NotNull Map<EquipmentSlot, ItemStack> equipment, float maxHealth) {
        this.key = key;
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.goalCreatorsGroups = List.copyOf(Objects.requireNonNull(goalCreatorsGroups, "goalCreatorsGroups"));
        this.triggers = Map.copyOf(Objects.requireNonNull(triggers, "triggers"));
        this.displayName = displayName;
        this.equipment = Map.copyOf(Objects.requireNonNull(equipment, "equipment"));
        this.maxHealth = maxHealth;
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    public @NotNull MinestomDescriptor getDescriptor() {
        return descriptor;
    }

    public @Unmodifiable @NotNull Collection<Collection<Goal>> getGoalGroups() {
        return goalCreatorsGroups;
    }

    public @Unmodifiable @NotNull Map<Key, Collection<Skill>> getTriggers() {
        return triggers;
    }

    public @NotNull Optional<Component> getDisplayName() {
        return Optional.ofNullable(displayName);
    }

    public @Unmodifiable @NotNull Map<EquipmentSlot, ItemStack> getEquipment() {
        return equipment;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

}
