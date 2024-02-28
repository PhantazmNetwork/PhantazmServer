package org.phantazm.mob2;

import com.github.steanky.proxima.path.Pathfinder;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.metadata.AgeableMobMeta;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.animal.tameable.WolfMeta;
import net.minestom.server.entity.metadata.item.ItemEntityMeta;
import net.minestom.server.entity.metadata.monster.zombie.ZombieMeta;
import net.minestom.server.entity.metadata.other.SlimeMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.goal.GoalApplier;
import org.phantazm.mob2.skill.Skill;
import org.phantazm.proxima.bindings.minestom.InstanceSpawner;
import org.phantazm.proxima.bindings.minestom.Pathfinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public class MobCreatorBase implements MobCreator {
    protected static final Logger LOGGER = LoggerFactory.getLogger(MobCreatorBase.class);

    protected final MobData data;
    private final ExtensionHolder extensionHolder;

    private final Pathfinding.Factory pathfinding;
    private final List<Skill> skills;
    private final List<GoalApplier> goalAppliers;

    private final Pathfinder pathfinder;
    private final Function<? super Instance, ? extends InstanceSpawner.InstanceSettings> settingsFunction;

    private final Map<EquipmentSlot, ItemStack> equipmentMap;
    private final Object2FloatMap<String> attributeMap;

    public MobCreatorBase(@NotNull MobData data, @NotNull ExtensionHolder extensionHolder, Pathfinding.@NotNull Factory pathfinding,
        @NotNull List<Skill> skills, @NotNull List<GoalApplier> goalAppliers, @NotNull Pathfinder pathfinder,
        @NotNull Function<? super Instance, ? extends InstanceSpawner.InstanceSettings> settingsFunction,
        @NotNull Map<EquipmentSlot, ItemStack> equipmentMap, @NotNull Object2FloatMap<String> attributeMap) {
        this.data = Objects.requireNonNull(data);
        this.extensionHolder = Objects.requireNonNull(extensionHolder);
        this.pathfinding = Objects.requireNonNull(pathfinding);
        this.skills = List.copyOf(skills);
        this.goalAppliers = List.copyOf(goalAppliers);

        this.pathfinder = Objects.requireNonNull(pathfinder);
        this.settingsFunction = Objects.requireNonNull(settingsFunction);

        this.equipmentMap = Map.copyOf(equipmentMap);
        this.attributeMap = Objects.requireNonNull(attributeMap);
    }

    @Override
    public @NotNull Mob create(@NotNull Instance instance, @NotNull ExtensionHolder extensionHolder) {
        InstanceSpawner.InstanceSettings settings = settingsFunction.apply(instance);

        Pathfinding pathfinding = this.pathfinding.make(pathfinder, settings.nodeLocal(), settings.spaceHandler());

        Mob mob = new Mob(data.type(), UUID.randomUUID(), pathfinding, data);
        mob.setExtensions(extensionHolder);
        mob.addSkills(skills);

        setup(mob);
        return mob;
    }

    @Override
    public @NotNull ExtensionHolder typeExtensions() {
        return extensionHolder;
    }

    protected void setup(@NotNull Mob mob) {
        setEquipment(mob);
        setAttributes(mob);
        setMeta(mob);
        setGoals(mob);
        setTeam(mob);
    }

    protected void setEquipment(@NotNull Mob mob) {
        for (Map.Entry<EquipmentSlot, ItemStack> entry : equipmentMap.entrySet()) {
            mob.setEquipment(entry.getKey(), entry.getValue());
        }
    }

    protected void setAttributes(@NotNull Mob mob) {
        for (Object2FloatArrayMap.Entry<String> entry : attributeMap.object2FloatEntrySet()) {
            Attribute attribute = Attribute.fromKey(entry.getKey());
            if (attribute != null) {
                mob.getAttribute(attribute).setBaseValue(entry.getFloatValue());
            }
        }

        mob.heal();
    }

    protected void setMeta(@NotNull Mob mob) {
        MobMeta dataMeta = data.meta();
        if (dataMeta == null) {
            return;
        }

        EntityMeta meta = mob.getEntityMeta();

        meta.setCustomName(dataMeta.customName());
        meta.setCustomNameVisible(dataMeta.customNameVisible());
        meta.setHasGlowingEffect(dataMeta.isGlowing());
        meta.setInvisible(dataMeta.isInvisible());

        if (meta instanceof AgeableMobMeta ageableMobMeta) {
            ageableMobMeta.setBaby(dataMeta.isBaby());
        }

        if (meta instanceof ZombieMeta zombieMeta) {
            zombieMeta.setBaby(dataMeta.isBaby());
        }

        if (meta instanceof WolfMeta wolfMeta) {
            wolfMeta.setAngerTime(dataMeta.angerTime());
        }

        if (meta instanceof SlimeMeta slimeMeta) {
            slimeMeta.setSize(dataMeta.size());
        }

        ItemStack itemStack = dataMeta.itemStack();
        if (itemStack != null && meta instanceof ItemEntityMeta itemMeta) {
            itemMeta.setItem(itemStack);
        }
    }

    protected void setGoals(@NotNull Mob mob) {
        for (GoalApplier applier : goalAppliers) {
            applier.apply(mob);
        }
    }

    protected void setTeam(@NotNull Mob mob) {
        if (!data.showNameTag()) {
            mob.setNameTagVisibility(TeamsPacket.NameTagVisibility.NEVER);
        }
    }
}
