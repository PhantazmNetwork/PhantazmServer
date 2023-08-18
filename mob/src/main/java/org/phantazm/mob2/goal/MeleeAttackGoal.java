package org.phantazm.mob2.goal;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.proxima.bindings.minestom.goal.ProximaGoal;

import java.util.Objects;

public class MeleeAttackGoal implements GoalCreator {
    private final Data data;

    @FactoryMethod
    public MeleeAttackGoal(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull ProximaGoal create(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Goal(data, mob);
    }

    private static class Goal implements ProximaGoal {
        private final Data data;
        private final Mob self;
        private final Attribute attribute;

        private int ticksSinceAttack = 0;

        public Goal(@NotNull Data data, @NotNull Mob self) {
            this.data = Objects.requireNonNull(data);
            this.self = Objects.requireNonNull(self);
            this.attribute = Attribute.fromKey("phantazm.attack_speed_multiplier");
        }

        @Override
        public boolean shouldStart() {
            float attackSpeedMultiplier = 1F;
            if (attribute != null) {
                attackSpeedMultiplier = self.getAttributeValue(attribute);
            }

            if ((float)ticksSinceAttack++ * attackSpeedMultiplier >= data.cooldown()) {
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
            ticksSinceAttack = 0;

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
                livingEntity.getAcquirable().sync(entity -> {
                    LivingEntity actualEntity = (LivingEntity)entity;
                    boolean damaged = actualEntity.damage(Damage.fromEntity(self, damageAmount), data.bypassArmor);

                    if (!damaged) {
                        return;
                    }

                    actualEntity.takeKnockback(knockbackStrength, data.horizontal, Math.sin(angle), -Math.cos(angle));
                });
            }
        }

        @Override
        public boolean shouldEnd() {
            return true;
        }
    }

    @DataObject
    public record Data(long cooldown, double range, boolean swingHand, boolean bypassArmor, boolean horizontal) {
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
