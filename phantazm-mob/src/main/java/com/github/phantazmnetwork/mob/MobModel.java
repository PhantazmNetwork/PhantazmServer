package com.github.phantazmnetwork.mob;

import com.github.phantazmnetwork.mob.skill.Skill;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.MinestomDescriptor;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntityFactory;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.Spawner;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MobModel<TDescriptor extends MinestomDescriptor, TEntity extends NeuralEntity> {

    private final TDescriptor descriptor;

    private final NeuralEntityFactory<? super TDescriptor, ? extends TEntity> entityFactory;

    private final Map<String, Skill> skills;

    private final Component displayName;

    private final Map<EquipmentSlot, ItemStack> equipment;

    public MobModel(@NotNull TDescriptor descriptor,
                    @NotNull NeuralEntityFactory<? super TDescriptor, ? extends TEntity> entityFactory,
                    @NotNull Map<String, Skill> skills, @Nullable Component displayName,
                    @NotNull Map<EquipmentSlot, ItemStack> equipment) {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.entityFactory = Objects.requireNonNull(entityFactory, "entityFactory");
        this.skills = Objects.requireNonNull(skills, "skills");
        this.displayName = displayName;
        this.equipment = Objects.requireNonNull(equipment, "equipment");
    }

    public @NotNull TDescriptor getDescriptor() {
        return descriptor;
    }

    public @NotNull Map<String, Skill> getSkills() {
        return Map.copyOf(skills);
    }

    public @NotNull Optional<Component> getDisplayName() {
        return Optional.ofNullable(displayName);
    }

    public @Unmodifiable @NotNull Map<EquipmentSlot, ItemStack> getEquipment() {
        return Map.copyOf(equipment);
    }

    public @NotNull TEntity spawn(@NotNull Spawner spawner, @NotNull Instance instance, @NotNull Point point) {
        return spawner.spawnEntity(instance, point, descriptor, entityFactory, this::onSpawn);
    }

    protected void onSpawn(@NotNull TEntity entity) {
        EntityMeta meta = entity.getEntityMeta();
        if (displayName != null) {
            meta.setCustomName(displayName);
            meta.setCustomNameVisible(true);
        }

        for (Map.Entry<EquipmentSlot, ItemStack> entry : equipment.entrySet()) {
            entity.setEquipment(entry.getKey(), entry.getValue());
        }
    }

}
