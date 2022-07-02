package com.github.phantazmnetwork.mob.spawner;

import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.goal.Goal;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.Spawner;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.GoalGroup;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.NeuralGoal;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Basic implementation of a {@link MobSpawner}.
 */
public class BasicMobSpawner implements MobSpawner {

    private final MobStore mobStore;

    private final Spawner neuralSpawner;

    /**
     * Creates a new {@link BasicMobSpawner}.
     * @param mobStore The {@link MobStore} to register new {@link PhantazmMob}s to
     * @param neuralSpawner The {@link Spawner} to spawn backing {@link NeuralEntity}s
     */
    public BasicMobSpawner(@NotNull MobStore mobStore, @NotNull Spawner neuralSpawner) {
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
        this.neuralSpawner = Objects.requireNonNull(neuralSpawner, "neuralSpawner");
    }

    @Override
    public @NotNull PhantazmMob spawn(@NotNull Instance instance, @NotNull Point point, @NotNull MobModel model) {
        NeuralEntity neuralEntity = neuralSpawner.spawnEntity(instance, point, model.getDescriptor());
        setDisplayName(neuralEntity, model);
        setEquipment(neuralEntity, model);
        setMaxHealth(neuralEntity, model);
        setSpeed(neuralEntity, model);

        PhantazmMob mob = new PhantazmMob(model, neuralEntity);
        addGoals(mob);

        mobStore.registerMob(mob);
        return mob;
    }

    private void setDisplayName(@NotNull NeuralEntity neuralEntity, @NotNull MobModel model) {
        model.getDisplayName().ifPresent(displayName -> {
            neuralEntity.setCustomName(displayName);
            neuralEntity.setCustomNameVisible(true);
        });
    }

    private void setEquipment(@NotNull NeuralEntity neuralEntity, @NotNull MobModel model) {
        for (Map.Entry<EquipmentSlot, ItemStack> entry : model.getEquipment().entrySet()) {
            neuralEntity.setEquipment(entry.getKey(), entry.getValue());
        }
    }

    private void setMaxHealth(@NotNull NeuralEntity neuralEntity, @NotNull MobModel model) {
        neuralEntity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(model.getMaxHealth());
        neuralEntity.setHealth(model.getMaxHealth());
    }

    private void setSpeed(@NotNull NeuralEntity neuralEntity, @NotNull MobModel model) {
        neuralEntity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(model.getSpeed());
    }

    private void addGoals(@NotNull PhantazmMob mob) {
        for (Collection<Goal> group : mob.model().getGoalGroups()) {
            Collection<NeuralGoal> neuralGroup = new ArrayList<>(group.size());
            for (Goal goal : group) {
                neuralGroup.add(goal.createGoal(mob));
            }

            GoalGroup goalGroup = new GoalGroup(neuralGroup);
            mob.entity().addGoalGroup(goalGroup);
        }
    }

}
