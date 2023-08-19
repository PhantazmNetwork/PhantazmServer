package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.potion.Potion;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.target.TargetSelector;

import java.util.Objects;

@Model("mob.skill.potion")
@Cache(false)
public class ApplyPotionSkill implements Skill {

    private final Data data;
    private final TargetSelector<Object> selector;

    @FactoryMethod
    public ApplyPotionSkill(@NotNull Data data, @NotNull @Child("selector") TargetSelector<Object> selector) {
        this.data = Objects.requireNonNull(data);
        this.selector = Objects.requireNonNull(selector);
    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        selector.selectTarget(self).ifPresent(livingEntity -> {
            if (livingEntity instanceof Iterable<?> iterable) {
                Iterable<LivingEntity> entityIterable = (Iterable<LivingEntity>) iterable;
                for (LivingEntity entity : entityIterable) {
                    entity.addEffect(data.potion());
                }
            } else if (livingEntity instanceof LivingEntity living) {
                living.addEffect(data.potion());
            }
        });
    }

    @DataObject
    public record Data(@NotNull @ChildPath("selector") String selector, @NotNull Potion potion) {

        public Data {
            Objects.requireNonNull(selector);
        }

    }

}
