package org.phantazm.mob;

import com.github.steanky.ethylene.core.collection.ConfigNode;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.mob.skill.Skill;
import org.phantazm.proxima.bindings.minestom.Pathfinding;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A model for the configuration of a {@link PhantazmMob}.
 */
public class MobModel implements Keyed {

    private final Key key;

    private final EntityType entityType;

    private final Pathfinding.Factory factory;

    private final ConfigNode node;

    private final ConfigNode metaNode;

    private final Component displayName;

    private final Map<EquipmentSlot, ItemStack> equipment;

    private final Object2FloatMap<String> attributes;

    /**
     * Creates a new mob model.
     *
     * @param key         The unique {@link Key} used to identify the mob
     * @param entityType  The type of entity this model represents
     * @param factory     The {@link Pathfinding.Factory} used to define the mob's navigation
     * @param displayName {@link Skill} The mob's display name, or null if it should not have one
     * @param equipment   The mob's equipment
     * @param attributes  The mob's attributes
     */
    public MobModel(@NotNull Key key, @NotNull EntityType entityType, @NotNull Pathfinding.Factory factory,
            @NotNull ConfigNode node, @NotNull ConfigNode metaNode, @Nullable Component displayName,
            @NotNull Map<EquipmentSlot, ItemStack> equipment, @NotNull Object2FloatMap<String> attributes) {
        this.key = Objects.requireNonNull(key, "key");
        this.entityType = Objects.requireNonNull(entityType, "entityType");
        this.factory = Objects.requireNonNull(factory, "factory");
        this.node = Objects.requireNonNull(node, "node");
        this.metaNode = Objects.requireNonNull(metaNode, "metaNode");
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

    public @NotNull EntityType getEntityType() {
        return entityType;
    }

    /**
     * Gets the {@link Pathfinding.Factory} used for the mob's navigation.
     *
     * @return The {@link Pathfinding.Factory} used for the mob's navigation
     */
    public @NotNull Pathfinding.Factory getFactory() {
        return factory;
    }

    public @NotNull ConfigNode getNode() {
        return node;
    }

    public @NotNull ConfigNode getMetaNode() {
        return metaNode;
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
