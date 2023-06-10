package org.phantazm.zombies.mob;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.annotation.Depend;
import com.github.steanky.element.core.annotation.Memoize;
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
import org.phantazm.mob.goal.GoalApplier;
import org.phantazm.mob.skill.Skill;
import org.phantazm.mob.spawner.MobSpawner;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;
import org.phantazm.proxima.bindings.minestom.Spawner;
import org.phantazm.zombies.map.objects.MapObjects;
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

    private static final ElementPath GOAL_APPLIERS_PATH = ElementPath.of("goalAppliers");
    private static final ElementPath TRIGGERS_PATH = ElementPath.of("triggers");

    private final Map<BooleanObjectPair<String>, ConfigProcessor<?>> processorMap;
    private final Spawner proximaSpawner;
    private final KeyParser keyParser;
    private final MobStore mobStore;
    private final DependencyProvider mobDependencyProvider;

    /**
     * Creates a new {@link BasicMobSpawner}.
     *
     * @param proximaSpawner The {@link Spawner} to spawn backing {@link ProximaEntity}s
     */
    public BasicMobSpawner(@NotNull Map<BooleanObjectPair<String>, ConfigProcessor<?>> processorMap,
            @NotNull Spawner proximaSpawner, @NotNull KeyParser keyParser, @NotNull Random random,
            @NotNull Supplier<? extends MapObjects> mapObjects, @NotNull MobStore mobStore) {
        this.processorMap = Map.copyOf(processorMap);
        this.proximaSpawner = Objects.requireNonNull(proximaSpawner, "neuralSpawner");
        this.keyParser = Objects.requireNonNull(keyParser, "keyParser");
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");

        this.mobDependencyProvider = new ModuleDependencyProvider(keyParser, new Module(this, mobStore,
                random, mapObjects));
    }

    @Override
    public @NotNull PhantazmMob spawn(@NotNull Instance instance, @NotNull Pos pos, @NotNull MobModel model) {
        ProximaEntity proximaEntity = proximaSpawner.spawn(instance, pos, model.getEntityType(), model.getFactory());

        setEntityMeta(proximaEntity, model);
        setEquipment(proximaEntity, model);
        setAttributes(proximaEntity, model);
        setHealth(proximaEntity);

        ElementContext context = model.getContext();
        Map<Key, Collection<Skill>> triggers = createTriggers(context);
        Collection<GoalApplier> goalAppliers = createGoalAppliers(context);

        PhantazmMob mob = new PhantazmMob(model, proximaEntity, triggers);
        for (GoalApplier applier : goalAppliers) {
            applier.apply(mob);
        }

        model.getHologramDisplayName().ifPresent(name -> {
            proximaEntity.setCustomName(name);
            proximaEntity.setCustomNameVisible(true);
        });

        this.mobStore.onMobSpawn(mob);

        for (Collection<Skill> skills : mob.triggers().values()) {
            for (Skill skill : skills) {
                skill.init(mob);
            }
        }

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

    private Map<Key, Collection<Skill>> createTriggers(ElementContext context) {
        ConfigNode node = context.root().getNodeOrDefault(LinkedConfigNode::new, "triggers");
        Map<Key, Collection<Skill>> skills = new HashMap<>(node.size());
        for (ConfigEntry entry : node.entryCollection()) {
            @Subst("key")
            String stringKey = entry.getKey();
            if (!keyParser.isValidKey(stringKey)) {
                continue;
            }

            Key key = keyParser.parseKey(stringKey);

            Collection<Skill> triggeredSkills =
                    context.provideCollection(TRIGGERS_PATH.append(stringKey), mobDependencyProvider, TRIGGER_HANDLER);

            if (!triggeredSkills.isEmpty()) {
                skills.put(key, triggeredSkills);
            }
        }

        return Map.copyOf(skills);
    }

    private Collection<GoalApplier> createGoalAppliers(ElementContext context) {
        return context.provideCollection(GOAL_APPLIERS_PATH, mobDependencyProvider, GOAL_HANDLER);
    }

    @Depend
    @Memoize
    public static class Module implements DependencyModule {
        private final MobSpawner spawner;
        private final MobStore mobStore;
        private final Random random;
        private final Supplier<? extends MapObjects> mapObjects;

        private Module(@NotNull MobSpawner spawner, @NotNull MobStore mobStore, @NotNull Random random,
                @NotNull Supplier<? extends MapObjects> mapObjects) {
            this.spawner = Objects.requireNonNull(spawner, "spawner");
            this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
            this.random = Objects.requireNonNull(random, "random");
            this.mapObjects = Objects.requireNonNull(mapObjects, "mapObjects");
        }

        public @NotNull MobSpawner getSpawner() {
            return spawner;
        }

        public @NotNull MobStore getMobStore() {
            return mobStore;
        }

        public Random getRandom() {
            return random;
        }

        public @NotNull Supplier<? extends MapObjects> mapObjects() {
            return mapObjects;
        }
    }
}
