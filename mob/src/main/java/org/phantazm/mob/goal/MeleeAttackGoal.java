package org.phantazm.mob.goal;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.MinecraftServer;
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
@Cache
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
            if ((System.currentTimeMillis() - lastAttackTime) / MinecraftServer.TICK_MS >= data.cooldown()) {
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
            if (target == null) {
                return;
            }

            self.attack(target, data.swingHand);
            if (target instanceof LivingEntity livingEntity) {
                Pos pos = self.getPosition();

                double angle = pos.yaw() * (Math.PI / 180);
                livingEntity.damage(DamageType.fromEntity(self), data.damageAmount);
                livingEntity.takeKnockback(0.4F * data.knockbackStrength, Math.sin(angle), -Math.cos(angle));

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
                       float damageAmount,
                       float knockbackStrength,
                       @NotNull @ChildPath("skills") Collection<String> skillPaths,
                       @NotNull @ChildPath("last_hit_selector") String lastHitSelectorPath) {

        public Data {
            Objects.requireNonNull(lastHitSelectorPath, "lastHitSelectorPath");
        }

    }
}
