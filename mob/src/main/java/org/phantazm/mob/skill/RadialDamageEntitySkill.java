package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
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
        this.data = Objects.requireNonNull(data);
        this.selector = Objects.requireNonNull(selector);
    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        selector.selectTarget(self).ifPresent(target -> {
            LivingEntity selfEntity = self.entity();
            if (target instanceof Iterable<?> iterable) {
                for (Object object : iterable) {
                    if (object instanceof LivingEntity entity) {
                        double damage = calculateDamage(entity.getDistance(selfEntity));
                        entity.damage(Damage.fromEntity(selfEntity, (float) damage), data.bypassArmor);
                    }
                }
            } else if (target instanceof LivingEntity living) {
                double damage = calculateDamage(living.getDistance(selfEntity));
                living.damage(Damage.fromEntity(selfEntity, (float) damage), data.bypassArmor);
            }
        });
    }

    private double calculateDamage(double distanceToEntity) {
        return ((data.damage * Math.sqrt(data.range)) / data.range) *
                   (Math.sqrt(Math.max(0, -distanceToEntity + data.range)));
    }

    @DataObject
    public record Data(@NotNull @ChildPath("selector") String selector,
        float damage,
        boolean bypassArmor,
        double range) {
    }
}
