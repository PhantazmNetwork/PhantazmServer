package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.target.TargetSelector;

import java.util.Objects;

@Model("mob.skill.radial_damage")
@Cache(false)
public class RadialDamageEntitySkill implements Skill {

    private final Data data;
    private final TargetSelector<Object> selector;

    @FactoryMethod
    public RadialDamageEntitySkill(@NotNull Data data, @NotNull @Child("selector") TargetSelector<Object> selector) {
        this.data = Objects.requireNonNull(data, "data");
        this.selector = Objects.requireNonNull(selector, "selector");
    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        selector.selectTarget(self).ifPresent(object -> {
            LivingEntity selfEntity = self.entity();
            if (object instanceof Iterable<?> iterable) {
                Iterable<LivingEntity> entityIterable = (Iterable<LivingEntity>)iterable;
                for (LivingEntity entity : entityIterable) {
                    double damage = calculateDamage(entity.getDistanceSquared(selfEntity));
                    entity.damage(DamageType.fromEntity(selfEntity), (float)damage, data.bypassArmor);
                }
            }
            else if (object instanceof LivingEntity living) {
                double damage = calculateDamage(living.getDistanceSquared(selfEntity));
                living.damage(DamageType.fromEntity(selfEntity), (float)damage, data.bypassArmor);
            }
        });
    }

    private double calculateDamage(double distanceToEntitySquared) {
        return Math.sqrt(Math.max(-distanceToEntitySquared + (data.range * data.range), 0));
    }

    @DataObject
    public record Data(@NotNull @ChildPath("selector") String selector,
                       float damage,
                       boolean bypassArmor,
                       double range) {

        public Data {
            Objects.requireNonNull(selector, "selector");
        }

    }

}