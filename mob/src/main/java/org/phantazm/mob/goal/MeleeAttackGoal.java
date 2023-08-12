package org.phantazm.mob.goal;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.thread.Acquired;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.Tags;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.SkillDelegatingGoal;
import org.phantazm.mob.skill.Skill;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;
import org.phantazm.proxima.bindings.minestom.goal.ProximaGoal;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@Model("mob.goal.melee_attack")
@Cache(false)
public class MeleeAttackGoal implements GoalCreator {
    private final Data data;
    private final Collection<Skill> skills;

    @FactoryMethod
    public MeleeAttackGoal(@NotNull Data data, @NotNull @Child("skills") Collection<Skill> skills) {
        this.data = Objects.requireNonNull(data, "data");
        this.skills = Objects.requireNonNull(skills, "skills");
    }

    @Override
    public @NotNull ProximaGoal create(@NotNull PhantazmMob mob) {
        return new Goal(data, skills, mob);
    }

    private static class Goal implements SkillDelegatingGoal {
        private final Data data;
        private final Collection<Skill> skills;
        private final PhantazmMob mob;

        private long ticksSinceAttack = 0;

        public Goal(@NotNull Data data, @NotNull Collection<Skill> skills, @NotNull PhantazmMob mob) {
            this.data = Objects.requireNonNull(data, "data");
            this.skills = Objects.requireNonNull(skills, "skills");
            this.mob = Objects.requireNonNull(mob, "mob");

            mob.entity().scheduler().scheduleTask(() -> {
                ++ticksSinceAttack;
            }, TaskSchedule.immediate(), TaskSchedule.nextTick(), ExecutionType.SYNC);

            Skill[] tickableSkills = skills.stream().filter(Skill::needsTicking).toArray(Skill[]::new);
            if (tickableSkills.length == 0) {
                return;
            }

            mob.entity().scheduler().scheduleTask(() -> {
                if (mob.entity().isDead() || mob.entity().isRemoved()) {
                    return;
                }

                long time = System.currentTimeMillis();
                for (Skill skill : tickableSkills) {
                    skill.tick(time, mob);
                }
            }, TaskSchedule.immediate(), TaskSchedule.nextTick(), ExecutionType.SYNC);
        }

        @Override
        public boolean shouldStart() {
            ProximaEntity self = mob.entity();

            Attribute attribute = Attribute.fromKey("phantazm.attack_speed_multiplier");
            float attackSpeedMultiplier = 1F;
            if (attribute != null) {
                attackSpeedMultiplier = self.getAttributeValue(attribute);
            }

            if ((float) ticksSinceAttack *
                    attackSpeedMultiplier >= data.cooldown()) {
                Entity target = self.getTargetEntity();
                if (target == null) {
                    return false;
                }

                return self.getBoundingBox().expand(data.range, data.range, data.range)
                        .intersectEntity(self.getPosition(), target);
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
                Acquired<Entity> entityAcquired = livingEntity.getAcquirable().lock();
                try {
                    LivingEntity actualEntity = (LivingEntity)entityAcquired.get();
                    boolean damaged = actualEntity.damage(Damage.fromEntity(self, damageAmount), data.bypassArmor);

                    if (!damaged) {
                        return;
                    }

                    actualEntity.takeKnockback(knockbackStrength, data.horizontal, Math.sin(angle), -Math.cos(angle));
                    self.setTag(Tags.LAST_MELEE_HIT_TAG, actualEntity.getUuid());

                    for (Skill skill : skills) {
                        skill.use(mob);
                    }
                }
                finally {
                    entityAcquired.unlock();
                }
            }

            ticksSinceAttack = 0;
        }

        @Override
        public boolean shouldEnd() {
            return true;
        }

        @Override
        public @NotNull @Unmodifiable Collection<Skill> skills() {
            return Collections.unmodifiableCollection(skills);
        }
    }

    @DataObject
    public record Data(long cooldown,
                       double range,
                       boolean swingHand,
                       boolean bypassArmor,
                       boolean horizontal,
                       @NotNull @ChildPath("skills") Collection<String> skillPaths) {
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
            return ConfigPrimitive.of(false);
        }
    }
}
