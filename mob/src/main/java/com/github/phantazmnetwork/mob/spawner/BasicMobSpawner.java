package com.github.phantazmnetwork.mob.spawner;

import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.skill.Skill;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.Spawner;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.GoalGroup;
import com.github.steanky.element.core.annotation.DependencySupplier;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.dependency.DependencyModule;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.dependency.ModuleDependencyProvider;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import net.kyori.adventure.key.Key;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Basic implementation of a {@link MobSpawner}.
 */
public class BasicMobSpawner implements MobSpawner {

    private final Map<BooleanObjectPair<String>, ConfigProcessor<?>> processorMap;

    private final MobStore mobStore;

    private final Spawner neuralSpawner;

    private final ContextManager contextManager;

    private final KeyParser keyParser;

    public static class Module implements DependencyModule {

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
    public BasicMobSpawner(@NotNull Map<BooleanObjectPair<String>, ConfigProcessor<?>> processorMap,
            @NotNull MobStore mobStore, @NotNull Spawner neuralSpawner, @NotNull ContextManager contextManager,
            @NotNull KeyParser keyParser) {
        this.processorMap = Map.copyOf(processorMap);
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
        this.neuralSpawner = Objects.requireNonNull(neuralSpawner, "neuralSpawner");
        this.contextManager = Objects.requireNonNull(contextManager, "contextManager");
        this.keyParser = Objects.requireNonNull(keyParser, "keyParser");
    }

    @Override
    public @NotNull PhantazmMob spawn(@NotNull Instance instance, @NotNull Point point, @NotNull MobModel model) {
        NeuralEntity neuralEntity = neuralSpawner.spawnEntity(instance, point, model.getDescriptor());
        setEntityMeta(neuralEntity, model);
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

    private void setEntityMeta(@NotNull NeuralEntity neuralEntity, @NotNull MobModel model) {
        EntityMeta meta = neuralEntity.getEntityMeta();
        ConfigNode metaNode = model.getMetaNode();
        for (Method method : meta.getClass().getMethods()) {
            if (!Modifier.isPublic(method.getModifiers()) || method.getReturnType() != void.class) {
                continue;
            }

            Parameter[] parameters = method.getParameters();
            if (parameters.length != 1) {
                continue;
            }

            String methodName = method.getName();
            if (!methodName.startsWith("set")) {
                continue;
            }
            String key;
            if (methodName.length() > 3) {
                key = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
            }
            else {
                key = methodName.substring(3);
            }

            ConfigElement element = metaNode.getElementOrDefault((ConfigElement)null, key);
            if (element == null) {
                continue;
            }

            Parameter parameter = parameters[0];
            NotNull[] notNulls = parameter.getAnnotationsByType(NotNull.class);
            Nullable[] nullables = parameter.getAnnotationsByType(Nullable.class);
            Class<?> type = parameter.getType();
            boolean optional = !type.isPrimitive() && nullables.length != 0 || notNulls.length < 1;

            ConfigProcessor<?> processor = processorMap.get(BooleanObjectPair.of(optional, type.getName()));
            if (processor == null) {
                continue;
            }

            Object data;
            try {
                data = processor.dataFromElement(element);
            }
            catch (ConfigProcessException e) {
                e.printStackTrace();
                continue;
            }
            if (data instanceof Optional<?> dataOptional) {
                data = dataOptional.orElse(null);
            }
            try {
                method.invoke(meta, data);
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
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
        Collection<GoalGroup> goalGroups =
                true ? Collections.emptyList() : context.provide("goalGroups", dependencyProvider);

        for (GoalGroup group : goalGroups) {
            entity.addGoalGroup(group);
        }
    }

    private @NotNull Map<Key, Collection<Skill>> createTriggers(@NotNull ElementContext context,
            @NotNull DependencyProvider dependencyProvider, @NotNull NeuralEntity entity, @NotNull MobModel model) {
        return true ? Collections.emptyMap() : context.provide("triggers", dependencyProvider);
    }

}
