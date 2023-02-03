package org.phantazm.zombies.mob;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.annotation.Depend;
import com.github.steanky.element.core.annotation.Memoize;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.dependency.DependencyModule;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.dependency.ModuleDependencyProvider;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.element.core.path.ElementPath;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigEntry;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import net.kyori.adventure.key.Key;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.core.ElementUtils;
import org.phantazm.mob.MobModel;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.skill.Skill;
import org.phantazm.mob.spawner.MobSpawner;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;
import org.phantazm.proxima.bindings.minestom.Spawner;
import org.phantazm.proxima.bindings.minestom.goal.GoalGroup;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Basic implementation of a {@link MobSpawner}.
 */
public class BasicMobSpawner implements MobSpawner {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicMobSpawner.class);

    private static final Consumer<? super ElementException> GOAL_HANDLER = ElementUtils.logging(LOGGER, "mob goal");
    private static final Consumer<? super ElementException> TRIGGER_HANDLER =
            ElementUtils.logging(LOGGER, "mob trigger");

    private final Map<BooleanObjectPair<String>, ConfigProcessor<?>> processorMap;

    private final Spawner proximaSpawner;

    private final ContextManager contextManager;

    private final KeyParser keyParser;

    private final Supplier<? extends MapObjects> mapObjects;

    /**
     * Creates a new {@link BasicMobSpawner}.
     *
     * @param proximaSpawner The {@link Spawner} to spawn backing {@link ProximaEntity}s
     */
    public BasicMobSpawner(@NotNull Map<BooleanObjectPair<String>, ConfigProcessor<?>> processorMap,
            @NotNull Spawner proximaSpawner, @NotNull ContextManager contextManager, @NotNull KeyParser keyParser,
            @NotNull Supplier<? extends MapObjects> mapObjects) {
        this.processorMap = Map.copyOf(processorMap);
        this.proximaSpawner = Objects.requireNonNull(proximaSpawner, "neuralSpawner");
        this.contextManager = Objects.requireNonNull(contextManager, "contextManager");
        this.keyParser = Objects.requireNonNull(keyParser, "keyParser");
        this.mapObjects = Objects.requireNonNull(mapObjects, "mapObjects");
    }

    @Override
    public @NotNull PhantazmMob spawn(@NotNull Instance instance, @NotNull Pos pos, @NotNull MobStore mobStore,
            @NotNull MobModel model) {
        ProximaEntity proximaEntity = proximaSpawner.spawn(instance, pos, model.getEntityType(), model.getFactory());

        setEntityMeta(proximaEntity, model);
        setEquipment(proximaEntity, model);
        setAttributes(proximaEntity, model);
        setHealth(proximaEntity);

        Module module = new Module(this, mobStore, model, proximaEntity, mapObjects);
        DependencyProvider dependencyProvider = new ModuleDependencyProvider(keyParser, module);
        ElementContext context = contextManager.makeContext(model.getNode());

        addGoals(context, dependencyProvider, proximaEntity);
        Map<Key, Collection<Skill>> triggers = createTriggers(context, dependencyProvider);

        PhantazmMob mob = new PhantazmMob(model, proximaEntity, triggers);
        mobStore.registerMob(mob);
        return mob;
    }

    private void setEntityMeta(@NotNull ProximaEntity neuralEntity, @NotNull MobModel model) {
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
            if (!methodName.startsWith("set") || methodName.length() < 4) {
                continue;
            }
            String key = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);

            ConfigElement element = metaNode.getElement(key);
            if (element == null) {
                continue;
            }

            Parameter parameter = parameters[0];
            NotNull notNull = parameter.getAnnotation(NotNull.class);
            Nullable nullable = parameter.getAnnotation(Nullable.class);
            Class<?> type = parameter.getType();
            boolean optional = !type.isPrimitive() && (nullable != null || notNull == null);

            ConfigProcessor<?> processor = processorMap.get(BooleanObjectPair.of(optional, type.getName()));
            if (processor == null) {
                continue;
            }

            Object data;
            try {
                data = processor.dataFromElement(element);
            }
            catch (ConfigProcessException e) {
                LOGGER.warn("Failed to process meta config for meta key '{}'", key, e);
                continue;
            }
            if (data instanceof Optional<?> dataOptional) {
                data = dataOptional.orElse(null);
            }
            try {
                method.invoke(meta, data);
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                LOGGER.warn("Failed to set meta value for meta key '{}' and method name '{}'", key, methodName, e);
            }
        }
    }

    private void setEquipment(@NotNull ProximaEntity neuralEntity, @NotNull MobModel model) {
        for (Map.Entry<EquipmentSlot, ItemStack> entry : model.getEquipment().entrySet()) {
            neuralEntity.setEquipment(entry.getKey(), entry.getValue());
        }
    }

    private void setAttributes(@NotNull ProximaEntity neuralEntity, @NotNull MobModel model) {
        for (Object2FloatArrayMap.Entry<String> entry : model.getAttributes().object2FloatEntrySet()) {
            Attribute attribute = Attribute.fromKey(entry.getKey());
            if (attribute != null) {
                neuralEntity.getAttribute(attribute).setBaseValue(entry.getFloatValue());
            }
        }
    }

    private void setHealth(@NotNull ProximaEntity entity) {
        entity.setHealth(entity.getAttributeValue(Attribute.MAX_HEALTH));
    }

    private void addGoals(@NotNull ElementContext context, @NotNull DependencyProvider dependencyProvider,
            @NotNull ProximaEntity entity) {
        Collection<GoalGroup> goalGroups =
                context.provideCollection(ElementPath.of("goalGroups"), dependencyProvider, GOAL_HANDLER);

        for (GoalGroup group : goalGroups) {
            entity.addGoalGroup(group);
        }
    }

    private @NotNull Map<Key, Collection<Skill>> createTriggers(@NotNull ElementContext context,
            @NotNull DependencyProvider dependencyProvider) {
        ConfigNode node = context.root().getNodeOrDefault(LinkedConfigNode::new, "triggers");
        Map<Key, Collection<Skill>> skills = new HashMap<>(node.size());
        for (ConfigEntry entry : node.entryCollection()) {
            @Subst("key")
            String stringKey = entry.getKey();
            if (!keyParser.isValidKey(stringKey)) {
                continue;
            }

            Key key = keyParser.parseKey(stringKey);
            skills.put(key, context.provideCollection(ElementPath.of("triggers").append(stringKey), dependencyProvider,
                    TRIGGER_HANDLER));
        }

        return skills;
    }

    @Depend
    @Memoize
    public static class Module implements DependencyModule {

        private final MobSpawner spawner;

        private final MobStore mobStore;

        private final MobModel model;

        private final ProximaEntity entity;

        private final Supplier<? extends MapObjects> mapObjects;

        private Module(@NotNull MobSpawner spawner, @NotNull MobStore mobStore, @NotNull MobModel model,
                @NotNull ProximaEntity entity, @NotNull Supplier<? extends MapObjects> mapObjects) {
            this.spawner = Objects.requireNonNull(spawner, "spawner");
            this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
            this.model = Objects.requireNonNull(model, "model");
            this.entity = Objects.requireNonNull(entity, "entity");
            this.mapObjects = Objects.requireNonNull(mapObjects, "mapObjects");
        }

        public @NotNull MobSpawner getSpawner() {
            return spawner;
        }

        public @NotNull MobStore getMobStore() {
            return mobStore;
        }

        public @NotNull MobModel getModel() {
            return model;
        }

        public @NotNull Entity getEntity() {
            return entity;
        }

        public @NotNull ProximaEntity getProximaEntity() {
            return entity;
        }

        public @NotNull Supplier<? extends MapObjects> mapObjects() {
            return mapObjects;
        }
    }
}
