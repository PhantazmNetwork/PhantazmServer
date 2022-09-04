package com.github.phantazmnetwork.mob.goal;

import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.NeuralGoal;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("mob.goal.melee_attack")
public class MeleeAttackGoal implements NeuralGoal {

    @DataObject
    public record Data(long cooldown, double rangeSquared) {

    }

    private final Data data;

    private final NeuralEntity entity;

    private long ticksSinceLastAttack = 0L;

    @FactoryMethod
    public MeleeAttackGoal(@NotNull Data data, @NotNull @Dependency("mob.entity.neural") NeuralEntity entity) {
        this.data = Objects.requireNonNull(data, "data");
        this.entity = Objects.requireNonNull(entity, "entity");
    }

    @Override
    public boolean shouldStart() {
        return true;
    }

    @Override
    public void tick(long time) {
        if (ticksSinceLastAttack >= data.cooldown()) {
            Entity target = entity.getTarget();
            if (target == null) {
                return;
            }

            double distance = entity.getDistanceSquared(target);
            if (distance <= data.rangeSquared()) {
                entity.attack(target, true);
                if (target instanceof LivingEntity livingEntity) {
                    float damage = entity.getAttributeValue(Attribute.ATTACK_DAMAGE);
                    livingEntity.damage(DamageType.fromEntity(entity), damage);

                    float knockback = entity.getAttributeValue(Attribute.ATTACK_KNOCKBACK);
                    Pos position = entity.getPosition();
                    double yaw = Math.toRadians(position.yaw());
                    livingEntity.takeKnockback(knockback, Math.sin(yaw), -Math.cos(yaw));
                }
                ticksSinceLastAttack = 0L;
            }
        }
        else {
            ticksSinceLastAttack++;
        }
    }

    @Override
    public boolean shouldEnd() {
        return false;
    }
}
