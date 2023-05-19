package org.phantazm.mob;

import com.github.steanky.element.core.context.ElementContext;
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
    private final ElementContext nodeContext;
    private final ConfigNode metaNode;
    private final Component displayName;
    private final Component hologramDisplayName;
    private final Map<EquipmentSlot, ItemStack> equipment;
    private final Object2FloatMap<String> attributes;

    public MobModel(@NotNull Key key, @NotNull EntityType entityType, @NotNull Pathfinding.Factory factory,
            @NotNull ElementContext nodeContext, @NotNull ConfigNode metaNode, @Nullable Component displayName,
            @Nullable Component hologramDisplayName, @NotNull Map<EquipmentSlot, ItemStack> equipment,
            @NotNull Object2FloatMap<String> attributes) {
        this.key = Objects.requireNonNull(key, "key");
        this.entityType = Objects.requireNonNull(entityType, "entityType");
        this.factory = Objects.requireNonNull(factory, "factory");
        this.nodeContext = Objects.requireNonNull(nodeContext, "nodeContext");
        this.metaNode = metaNode.immutableCopy();
        this.displayName = displayName;
        this.hologramDisplayName = hologramDisplayName;
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

    public @NotNull ElementContext getContext() {
        return nodeContext;
    }

    public @NotNull ConfigNode getMetaNode() {
        return metaNode;
    }

    /**
     * Gets the mob's display name, or null if it should not have one. This is the name that is used to refer to the
     * entity in chat messages.
     *
     * @return The mob's display name, or null if it should not have one
     */
    public @NotNull Optional<Component> getDisplayName() {
        return Optional.ofNullable(displayName);
    }

    public @NotNull Optional<Component> getHologramDisplayName() {
        return Optional.ofNullable(hologramDisplayName);
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
