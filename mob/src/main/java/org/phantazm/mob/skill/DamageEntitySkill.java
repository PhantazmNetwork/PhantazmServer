package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.target.TargetSelector;

import java.util.Objects;

@Model("mob.skill.damage")
@Cache(false)
public class DamageEntitySkill implements Skill {
    private final Data data;
    private final TargetSelector<Object> selector;

    @FactoryMethod
    public DamageEntitySkill(@NotNull Data data, @NotNull @Child("selector") TargetSelector<Object> selector) {
        this.data = Objects.requireNonNull(data);
        this.selector = Objects.requireNonNull(selector);
    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        selector.selectTarget(self).ifPresent(livingEntity -> {
            if (livingEntity instanceof Iterable<?> iterable) {
                for (Object object : iterable) {
                    if (object instanceof LivingEntity entity) {
                        entity.damage(Damage.fromEntity(self.entity(), data.damage()), data.bypassArmor);
                    }
                }
            } else if (livingEntity instanceof LivingEntity living) {
                living.damage(Damage.fromEntity(self.entity(), data.damage()), data.bypassArmor);
            }
        });
    }

    @DataObject
    public record Data(@NotNull @ChildPath("selector") String selector,
        float damage,
        boolean bypassArmor) {
    }
}
