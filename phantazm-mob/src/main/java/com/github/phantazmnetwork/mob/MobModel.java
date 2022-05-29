package com.github.phantazmnetwork.mob;

import com.github.phantazmnetwork.commons.IteratorUtils;
import com.github.phantazmnetwork.mob.goal.GoalCreator;
import com.github.phantazmnetwork.mob.skill.Skill;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.MinestomDescriptor;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.Spawner;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.GoalGroup;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.NeuralGoal;
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

    private final Iterable<Iterable<GoalCreator<?>>> goalCreatorsGroups;

    private final Component displayName;

    private final Map<EquipmentSlot, ItemStack> equipment;

    public MobModel(@NotNull MinestomDescriptor descriptor, @NotNull Map<String, ? extends Skill> skills,
                    @NotNull Iterable<Iterable<GoalCreator<?>>> goalCreatorsGroups, @Nullable Component displayName,
                    @NotNull Map<EquipmentSlot, ItemStack> equipment) {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.skills = Objects.requireNonNull(skills, "skills");
        this.goalCreatorsGroups = Objects.requireNonNull(goalCreatorsGroups, "goalCreatorsGroups");
        this.displayName = displayName;
        this.equipment = Objects.requireNonNull(equipment, "equipment");
    }

    public @NotNull MinestomDescriptor getDescriptor() {
        return descriptor;
    }

    public @Unmodifiable @NotNull Map<String, Skill> getSkills() {
        return Map.copyOf(skills);
    }

    public @Unmodifiable @NotNull Iterable<Iterable<GoalCreator<?>>> getGoalCreatorsGroups() {
        return () -> new Iterator<>() {

            private final Iterator<Iterable<GoalCreator<?>>> iterableIterator = goalCreatorsGroups.iterator();

            @Override
            public boolean hasNext() {
                return iterableIterator.hasNext();
            }

            @Override
            public Iterable<GoalCreator<?>> next() {
                return () -> IteratorUtils.unmodifiable(iterableIterator.next().iterator());
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
        for (Iterable<GoalCreator<?>> creators : goalCreatorsGroups) {
            Collection<NeuralGoal> goals = new ArrayList<>();
            for (GoalCreator<?> creator : creators) {
                goals.add(creator.createGoal(mob));
            }

            mob.entity().addGoalGroup(new GoalGroup(goals));
        }
    }

}
