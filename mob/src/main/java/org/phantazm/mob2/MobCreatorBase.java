package org.phantazm.mob2;

import com.github.steanky.proxima.path.Pathfinder;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import net.kyori.adventure.key.Key;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.goal.GoalApplier;
import org.phantazm.mob2.skill.SkillComponent;
import org.phantazm.proxima.bindings.minestom.Pathfinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

public class MobCreatorBase implements MobCreator {
    protected static final Logger LOGGER = LoggerFactory.getLogger(MobCreatorBase.class);

    protected final MobData data;

    private final Pathfinding.Factory pathfinding;
    private final List<SkillComponent> skills;
    private final List<GoalApplier> goalAppliers;

    private final Pathfinder pathfinder;
    private final Function<? super Instance, ? extends InstanceSettings> settingsFunction;

    public MobCreatorBase(@NotNull MobData data, Pathfinding.@NotNull Factory pathfinding,
            @NotNull List<SkillComponent> skills, @NotNull List<GoalApplier> goalAppliers,
            @NotNull Pathfinder pathfinder,
            @NotNull Function<? super @NotNull Instance, ? extends @NotNull InstanceSettings> settingsFunction) {
        this.data = Objects.requireNonNull(data);
        this.pathfinding = Objects.requireNonNull(pathfinding);
        this.skills = List.copyOf(skills);
        this.goalAppliers = List.copyOf(goalAppliers);

        this.pathfinder = Objects.requireNonNull(pathfinder);
        this.settingsFunction = Objects.requireNonNull(settingsFunction);
    }

    @Override
    public @NotNull Mob create(@NotNull Key key, @NotNull Instance instance) {
        InstanceSettings settings = settingsFunction.apply(instance);

        Pathfinding pathfinding = this.pathfinding.make(pathfinder, settings.nodeLocal(), settings.spaceHandler());
        Mob mob = new Mob(data.type(), UUID.randomUUID(), pathfinding, data);

        InjectionStore store = store();
        for (SkillComponent component : skills) {
            mob.addSkill(component.apply(mob, store));
        }

        setup(mob);
        return mob;
    }

    public @NotNull InjectionStore store() {
        return InjectionStore.EMPTY;
    }

    protected void setup(@NotNull Mob mob) {
        setDisplayName(mob);
        setEquipment(mob);
        setAttributes(mob);
        setMeta(mob);
        setGoals(mob);
    }

    protected void setDisplayName(@NotNull Mob mob) {
        data.hologramDisplayName().ifPresent(component -> {
            mob.setCustomName(component);
            mob.setCustomNameVisible(true);
        });
    }

    protected void setEquipment(@NotNull Mob mob) {
        for (Map.Entry<EquipmentSlot, ItemStack> entry : data.equipment().entrySet()) {
            mob.setEquipment(entry.getKey(), entry.getValue());
        }
    }

    protected void setAttributes(@NotNull Mob mob) {
        for (Object2FloatArrayMap.Entry<String> entry : data.attributes().object2FloatEntrySet()) {
            Attribute attribute = Attribute.fromKey(entry.getKey());
            if (attribute != null) {
                mob.getAttribute(attribute).setBaseValue(entry.getFloatValue());
            }
        }

        mob.setHealth(mob.getAttributeValue(Attribute.MAX_HEALTH));
    }

    protected void setMeta(@NotNull Mob mob) {
        EntityMeta meta = mob.getEntityMeta();

        for (Method method : meta.getClass().getMethods()) {
            if (!Modifier.isPublic(method.getModifiers()) || method.getReturnType() != void.class) {
                continue;
            }

            if (method.getParameterCount() != 1) {
                continue;
            }

            String methodName = method.getName();
            if (!methodName.startsWith("set") || methodName.length() < 4) {
                continue;
            }

            String key = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
            Object object = data.meta().get(key);
            if (object == null) {
                continue;
            }

            try {
                method.invoke(meta, object);
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                LOGGER.warn("failed to set meta value for meta key {} and method name {}", key, methodName, e);
            }
        }
    }

    protected void setGoals(@NotNull Mob mob) {
        for (GoalApplier applier : goalAppliers) {
            applier.apply(mob, store());
        }
    }
}
