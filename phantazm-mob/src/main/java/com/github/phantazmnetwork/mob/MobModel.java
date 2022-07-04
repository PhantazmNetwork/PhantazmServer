package com.github.phantazmnetwork.mob;

import com.github.phantazmnetwork.mob.goal.Goal;
import com.github.phantazmnetwork.mob.skill.Skill;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.MinestomDescriptor;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

/**
 * A model for the configuration of a {@link PhantazmMob}.
 */
public class MobModel implements Keyed {

    private final Key key;

    private final MinestomDescriptor descriptor;

    private final Collection<Collection<Goal>> goalGroups;

    private final Map<Key, Collection<Skill>> triggers;

    private final Component displayName;

    private final Map<EquipmentSlot, ItemStack> equipment;

    private final Object2FloatMap<String> attributes;

    /**
     * Creates a new mob model.
     * @param key The unique {@link Key} used to identify the mob
     * @param descriptor The {@link MinestomDescriptor} used for the mob's navigation
     * @param goalGroups Groups of {@link Goal}s to add
     * @param triggers {@link Skill}s associated with triggers
     * @param displayName {@link Skill} The mob's display name, or null if it should not have one
     * @param equipment The mob's equipment
     * @param maxHealth The mob's maximum health
     */
    public MobModel(@NotNull Key key, @NotNull MinestomDescriptor descriptor,
                    @NotNull Collection<Collection<Goal>> goalGroups,
                    @NotNull Map<Key, Collection<Skill>> triggers, @Nullable Component displayName,
                    @NotNull Map<EquipmentSlot, ItemStack> equipment, @NotNull Object2FloatMap<String> attributes) {
        this.key = Objects.requireNonNull(key, "key");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.goalGroups = List.copyOf(Objects.requireNonNull(goalGroups, "goalGroups"));
        this.triggers = Map.copyOf(Objects.requireNonNull(triggers, "triggers"));
        this.displayName = displayName;
        this.equipment = Map.copyOf(Objects.requireNonNull(equipment, "equipment"));
        this.attributes = Object2FloatMaps.unmodifiable(Objects.requireNonNull(attributes, "attributes"));
    }

    /**
     * Gets the unique {@link Key} used to identify the mob.
     * @return The unique {@link Key} used to identify the mob
     */
    @Override
    public @NotNull Key key() {
        return key;
    }

    /**
     * Gets the {@link MinestomDescriptor} used for the mob's navigation.
     * @return The {@link MinestomDescriptor} used for the mob's navigation
     */
    public @NotNull MinestomDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Gets the groups of {@link Goal}s to add.
     * @return The groups of {@link Goal}s to add
     */
    public @Unmodifiable @NotNull Collection<Collection<Goal>> getGoalGroups() {
        return goalGroups;
    }

    /**
     * Gets the {@link Skill}s associated with triggers.
     * @return The {@link Skill}s associated with triggers
     */
    public @Unmodifiable @NotNull Map<Key, Collection<Skill>> getTriggers() {
        return triggers;
    }

    /**
     * Gets the mob's display name, or null if it should not have one.
     * @return The mob's display name, or null if it should not have one
     */
    public @NotNull Optional<Component> getDisplayName() {
        return Optional.ofNullable(displayName);
    }

    /**
     * Gets the mob's equipment.
     * @return The mob's equipment
     */
    public @Unmodifiable @NotNull Map<EquipmentSlot, ItemStack> getEquipment() {
        return equipment;
    }

    public @NotNull @Unmodifiable Object2FloatMap<String> getAttributes() {
        return attributes;
    }
}
