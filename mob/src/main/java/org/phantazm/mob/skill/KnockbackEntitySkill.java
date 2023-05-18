package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.target.TargetSelector;

import java.util.Objects;

@Model("mob.skill.knockback")
@Cache(false)
public class KnockbackEntitySkill implements Skill {
    private final Data data;
    private final TargetSelector<? extends LivingEntity> selector;

    @FactoryMethod
    public KnockbackEntitySkill(@NotNull Data data,
            @NotNull @Child("selector") TargetSelector<? extends LivingEntity> selector) {
        this.data = Objects.requireNonNull(data, "data");
        this.selector = Objects.requireNonNull(selector, "selector");
    }

    @Override
    public void tick(long time, @NotNull PhantazmMob self) {

    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        selector.selectTarget(self).ifPresent(livingEntity -> {
            Pos position = self.entity().getPosition();
            double yaw = Math.toRadians(position.yaw());

            livingEntity.takeKnockback(data.knockback(), Math.sin(yaw), -Math.cos(yaw));
        });
    }

    @DataObject
    public record Data(@NotNull @ChildPath("selector") String selectorPath, float knockback) {

        public Data {
            Objects.requireNonNull(selectorPath, "selectorPath");
        }

    }

}
