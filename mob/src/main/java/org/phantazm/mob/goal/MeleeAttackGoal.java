package org.phantazm.mob.goal;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.skill.Skill;
import org.phantazm.mob.target.LastHitSelector;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;
import org.phantazm.proxima.bindings.minestom.goal.ProximaGoal;

import java.util.Collection;
import java.util.Objects;

@Model("mob.goal.melee_attack")
@Cache(false)
public class MeleeAttackGoal implements GoalCreator {
    private final Data data;
    private final Collection<Skill> skills;
    private final LastHitSelector<LivingEntity> lastHitSelector;

    @FactoryMethod
    public MeleeAttackGoal(@NotNull Data data, @NotNull @Child("skills") Collection<Skill> skills,
            @NotNull @Child("last_hit_selector") LastHitSelector<LivingEntity> lastHitSelector) {
        this.data = Objects.requireNonNull(data, "data");
        this.skills = Objects.requireNonNull(skills, "skills");
        this.lastHitSelector = Objects.requireNonNull(lastHitSelector, "lastHitSelector");
    }

    @Override
    public @NotNull ProximaGoal create(@NotNull PhantazmMob mob) {
        return new Goal(data, skills, lastHitSelector, mob);
    }

    private static class Goal implements ProximaGoal {
        private final Data data;
        private final Collection<Skill> skills;
        private final LastHitSelector<LivingEntity> lastHitSelector;
        private final PhantazmMob mob;

        private long lastAttackTime;

        @FactoryMethod
        public Goal(@NotNull Data data, @NotNull Collection<Skill> skills,
                @NotNull LastHitSelector<LivingEntity> lastHitSelector, @NotNull PhantazmMob mob) {
            this.data = Objects.requireNonNull(data, "data");
            this.skills = Objects.requireNonNull(skills, "skills");
            this.lastHitSelector = Objects.requireNonNull(lastHitSelector, "lastHitSelector");
            this.mob = Objects.requireNonNull(mob, "mob");
        }

        @Override
        public boolean shouldStart() {
            ProximaEntity self = mob.entity();

            Attribute attribute = Attribute.fromKey("phantazm.attack_speed_multiplier");
            float attackSpeedMultiplier = 1F;
            if (attribute != null) {
                attackSpeedMultiplier = self.getAttributeValue(attribute);
            }

            if ((float)((System.currentTimeMillis() - lastAttackTime) / MinecraftServer.TICK_MS) *
                    attackSpeedMultiplier >= data.cooldown()) {
                Entity target = self.getTargetEntity();
                if (target == null) {
                    return false;
                }

                return self.getDistanceSquared(target) <= data.range * data.range;
            }

            return false;
        }

        @Override
        public void start() {
            ProximaEntity self = mob.entity();
            Entity target = self.getTargetEntity();
            if (target == null || self.isDead()) {
                return;
            }

            self.attack(target, data.swingHand);
            if (target instanceof LivingEntity livingEntity) {
                Pos pos = self.getPosition();

                float damageAmount = self.getAttributeValue(Attribute.ATTACK_DAMAGE);
                float knockbackStrength = self.getAttributeValue(Attribute.ATTACK_KNOCKBACK);

                double angle = pos.yaw() * (Math.PI / 180);
                boolean damaged = livingEntity.damage(DamageType.fromEntity(self), damageAmount, data.bypassArmor);

                if (damaged) {
                    livingEntity.takeKnockback(knockbackStrength, data.horizontal, Math.sin(angle), -Math.cos(angle));
                }

                lastHitSelector.setLastHit(livingEntity);

                for (Skill skill : skills) {
                    skill.use(mob);
                }
            }

            lastAttackTime = System.currentTimeMillis();
        }

        @Override
        public boolean shouldEnd() {
            return true;
        }
    }

    @DataObject
    public record Data(long cooldown,
                       double range,
                       boolean swingHand,
                       boolean bypassArmor,
                       boolean horizontal,
                       @NotNull @ChildPath("skills") Collection<String> skillPaths,
                       @NotNull @ChildPath("last_hit_selector") String lastHitSelectorPath) {

        public Data {
            Objects.requireNonNull(lastHitSelectorPath, "lastHitSelectorPath");
        }

        @Default("swingHand")
        public static @NotNull ConfigElement defaultSwingHand() {
            return ConfigPrimitive.of(true);
        }

        @Default("bypassArmor")
        public static @NotNull ConfigElement defaultBypassArmor() {
            return ConfigPrimitive.of(false);
        }

        @Default("horizontal")
        public static @NotNull ConfigElement defaultHorizontal() {
            return ConfigPrimitive.of(true);
        }
    }
}
