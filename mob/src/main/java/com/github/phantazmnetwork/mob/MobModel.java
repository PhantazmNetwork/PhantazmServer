package com.github.phantazmnetwork.mob;

import com.github.phantazmnetwork.mob.skill.Skill;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.MinestomDescriptor;
import com.github.steanky.ethylene.core.collection.ConfigNode;
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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A model for the configuration of a {@link PhantazmMob}.
 */
public class MobModel implements Keyed {

    private final Key key;

    private final MinestomDescriptor descriptor;

    private final ConfigNode node;

    private final Component displayName;

    private final Map<EquipmentSlot, ItemStack> equipment;

    private final Object2FloatMap<String> attributes;

    /**
     * Creates a new mob model.
     *
     * @param key         The unique {@link Key} used to identify the mob
     * @param descriptor  The {@link MinestomDescriptor} used for the mob's navigation
     * @param displayName {@link Skill} The mob's display name, or null if it should not have one
     * @param equipment   The mob's equipment
     * @param attributes  The mob's attributes
     */
    public MobModel(@NotNull Key key, @NotNull MinestomDescriptor descriptor, @NotNull ConfigNode node,
            @Nullable Component displayName, @NotNull Map<EquipmentSlot, ItemStack> equipment,
            @NotNull Object2FloatMap<String> attributes) {
        this.key = Objects.requireNonNull(key, "key");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.node = Objects.requireNonNull(node, "node");
        this.displayName = displayName;
        this.equipment = Map.copyOf(Objects.requireNonNull(equipment, "equipment"));
        this.attributes = Object2FloatMaps.unmodifiable(Objects.requireNonNull(attributes, "attributes"));
    }

    /**
     * Gets the unique {@link Key} used to identify the mob.
     *
     * @return The unique {@link Key} used to identify the mob
     */
    @Override
    public @NotNull Key key() {
        return key;
    }

    /**
     * Gets the {@link MinestomDescriptor} used for the mob's navigation.
     *
     * @return The {@link MinestomDescriptor} used for the mob's navigation
     */
    public @NotNull MinestomDescriptor getDescriptor() {
        return descriptor;
    }

    public @NotNull ConfigNode getNode() {
        return node;
    }

    /**
     * Gets the mob's display name, or null if it should not have one.
     *
     * @return The mob's display name, or null if it should not have one
     */
    public @NotNull Optional<Component> getDisplayName() {
        return Optional.ofNullable(displayName);
    }

    /**
     * Gets the mob's equipment.
     *
     * @return The mob's equipment
     */
    public @Unmodifiable @NotNull Map<EquipmentSlot, ItemStack> getEquipment() {
        return equipment;
    }

    /**
     * Gets the mob's attributes.
     *
     * @return The mob's attributes
     */
    public @NotNull @Unmodifiable Object2FloatMap<String> getAttributes() {
        return attributes;
    }
}
