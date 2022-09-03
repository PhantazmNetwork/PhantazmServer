package com.github.phantazmnetwork.mob.spawner;

import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.skill.Skill;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.Spawner;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.GoalGroup;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.NeuralGoal;
import com.github.steanky.element.core.annotation.DependencySupplier;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.dependency.DependencyModule;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.dependency.ModuleDependencyProvider;
import com.github.steanky.element.core.key.KeyParser;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import net.kyori.adventure.key.Key;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Basic implementation of a {@link MobSpawner}.
 */
public class BasicMobSpawner implements MobSpawner {

    private final MobStore mobStore;

    private final Spawner neuralSpawner;

    private final ContextManager contextManager;

    private final KeyParser keyParser;

    @SuppressWarnings("ClassCanBeRecord")
    private static class Module implements DependencyModule {

        private final NeuralEntity entity;

        public Module(@NotNull NeuralEntity entity) {
            this.entity = Objects.requireNonNull(entity, "entity");
        }

        @DependencySupplier("mob.entity.entity")
        public @NotNull Entity getEntity() {
            return entity;
        }

        @DependencySupplier("mob.entity.neural_entity")
        public @NotNull NeuralEntity getNeuralEntity() {
            return entity;
        }

    }

    /**
     * Creates a new {@link BasicMobSpawner}.
     *
     * @param mobStore      The {@link MobStore} to register new {@link PhantazmMob}s to
     * @param neuralSpawner The {@link Spawner} to spawn backing {@link NeuralEntity}s
     */
    public BasicMobSpawner(@NotNull MobStore mobStore, @NotNull Spawner neuralSpawner,
            @NotNull ContextManager contextManager, @NotNull KeyParser keyParser) {
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
        this.neuralSpawner = Objects.requireNonNull(neuralSpawner, "neuralSpawner");
        this.contextManager = Objects.requireNonNull(contextManager, "contextManager");
        this.keyParser = Objects.requireNonNull(keyParser, "keyParser");
    }

    @Override
    public @NotNull PhantazmMob spawn(@NotNull Instance instance, @NotNull Point point, @NotNull MobModel model) {
        NeuralEntity neuralEntity = neuralSpawner.spawnEntity(instance, point, model.getDescriptor());
        setDisplayName(neuralEntity, model);
        setEquipment(neuralEntity, model);
        setAttributes(neuralEntity, model);
        setHealth(neuralEntity, model);

        Module module = new Module(neuralEntity);
        DependencyProvider dependencyProvider = new ModuleDependencyProvider(module, keyParser);
        ElementContext context = contextManager.makeContext(model.getNode());
        addGoals(context, dependencyProvider, neuralEntity, model);
        Map<Key, Collection<Skill>> triggers = createTriggers(context, dependencyProvider, neuralEntity, model);

        PhantazmMob mob = new PhantazmMob(model, neuralEntity, triggers);
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

    private void setAttributes(@NotNull NeuralEntity neuralEntity, @NotNull MobModel model) {
        for (Object2FloatArrayMap.Entry<String> entry : model.getAttributes().object2FloatEntrySet()) {
            Attribute attribute = Attribute.fromKey(entry.getKey());
            if (attribute != null) {
                neuralEntity.getAttribute(attribute).setBaseValue(entry.getFloatValue());
            }
        }
    }

    private void setHealth(@NotNull NeuralEntity entity, @NotNull MobModel model) {
        entity.setHealth(entity.getAttributeValue(Attribute.MAX_HEALTH));
    }


    private void addGoals(@NotNull ElementContext context, @NotNull DependencyProvider dependencyProvider,
            @NotNull NeuralEntity entity, @NotNull MobModel model) {
        Collection<Collection<NeuralGoal>> goalGroups = context.provide("goalGroups", dependencyProvider);

        for (Collection<NeuralGoal> group : goalGroups) {
            entity.addGoalGroup(new GoalGroup(group));
        }
    }

    private @NotNull Map<Key, Collection<Skill>> createTriggers(@NotNull ElementContext context,
            @NotNull DependencyProvider dependencyProvider, @NotNull NeuralEntity entity, @NotNull MobModel model) {
        return context.provide("triggers", dependencyProvider);
    }

}
