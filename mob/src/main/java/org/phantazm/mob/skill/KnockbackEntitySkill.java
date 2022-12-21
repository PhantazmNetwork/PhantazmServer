package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.target.TargetSelector;

import java.util.Objects;

@Model("mob.skill.knockback")
public class KnockbackEntitySkill implements Skill {

    private final Data data;
    private final Entity user;
    private final TargetSelector<? extends LivingEntity> selector;

    @FactoryMethod
    public KnockbackEntitySkill(@NotNull Data data, @NotNull @Dependency("mob.entity.entity") Entity user,
            @NotNull @DataName("selector") TargetSelector<? extends LivingEntity> selector) {
        this.data = Objects.requireNonNull(data, "data");
        this.user = Objects.requireNonNull(user, "user");
        this.selector = Objects.requireNonNull(selector, "selector");
    }

    @Override
    public void tick(long time) {

    }

    @Override
    public void use() {
        selector.selectTarget().ifPresent(livingEntity -> {
            Pos position = user.getPosition();
            double yaw = Math.toRadians(position.yaw());

            livingEntity.takeKnockback(data.knockback(), Math.sin(yaw), -Math.cos(yaw));
        });
    }

    @DataObject
    public record Data(@NotNull @DataPath("selector") String selectorPath, float knockback) {

        public Data {
            Objects.requireNonNull(selectorPath, "selectorPath");
        }

    }

}
