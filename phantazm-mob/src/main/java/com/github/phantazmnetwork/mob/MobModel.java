package com.github.phantazmnetwork.mob;

import com.github.phantazmnetwork.commons.IteratorUtils;
import com.github.phantazmnetwork.mob.goal.GoalGroupCreator;
import com.github.phantazmnetwork.mob.skill.Skill;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.MinestomDescriptor;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;
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

import java.util.*;

public class MobModel {

    private final MinestomDescriptor descriptor;

    private final Map<String, ? extends Skill> skills;

    private final Iterable<GoalGroupCreator> goalGroupCreators;

    private final Component displayName;

    private final Map<EquipmentSlot, ItemStack> equipment;

    public MobModel(@NotNull MinestomDescriptor descriptor, @NotNull Map<String, ? extends Skill> skills,
                    @NotNull Iterable<GoalGroupCreator> goalGroupCreators, @Nullable Component displayName,
                    @NotNull Map<EquipmentSlot, ItemStack> equipment) {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.skills = Objects.requireNonNull(skills, "skills");
        this.goalGroupCreators = Objects.requireNonNull(goalGroupCreators, "goalGroupCreators");
        this.displayName = displayName;
        this.equipment = Objects.requireNonNull(equipment, "equipment");
    }

    public @NotNull MinestomDescriptor getDescriptor() {
        return descriptor;
    }

    public @Unmodifiable @NotNull Map<String, Skill> getSkills() {
        return Map.copyOf(skills);
    }

    public @Unmodifiable @NotNull Iterable<GoalGroupCreator> getGoalGroupCreators() {
        return new Iterable<>() {

            private final Iterator<GoalGroupCreator> iterator
                    = IteratorUtils.unmodifiable(goalGroupCreators.iterator());

            @NotNull
            @Override
            public Iterator<GoalGroupCreator> iterator() {
                return iterator;
            }
        };
    }

    public @NotNull Optional<Component> getDisplayName() {
        return Optional.ofNullable(displayName);
    }

    public @Unmodifiable @NotNull Map<EquipmentSlot, ItemStack> getEquipment() {
        return Map.copyOf(equipment);
    }

    public @NotNull PhantazmMob spawn(@NotNull Spawner spawner, @NotNull Instance instance,
                                      @NotNull Point point) {
        PhantazmMob mob = new PhantazmMob(this, spawner.spawnEntity(instance, point, descriptor, this::onSpawn));
        postSpawn(mob);

        return mob;
    }

    protected void onSpawn(@NotNull NeuralEntity entity) {
        EntityMeta meta = entity.getEntityMeta();
        if (displayName != null) {
            meta.setCustomName(displayName);
            meta.setCustomNameVisible(true);
        }

        for (Map.Entry<EquipmentSlot, ItemStack> entry : equipment.entrySet()) {
            entity.setEquipment(entry.getKey(), entry.getValue());
        }
    }

    protected void postSpawn(@NotNull PhantazmMob mob) {
        for (GoalGroupCreator creator : goalGroupCreators) {
            mob.entity().addGoalGroup(creator.createGoalGroup(mob));
        }
    }

}
