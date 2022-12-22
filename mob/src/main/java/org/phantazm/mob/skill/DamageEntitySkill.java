package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.target.TargetSelector;

import java.util.Objects;

@Model("mob.skill.damage")
public class DamageEntitySkill implements Skill {

    private final Data data;
    private final DamageType damageType;
    private final TargetSelector<? extends LivingEntity> selector;

    @FactoryMethod
    public DamageEntitySkill(@NotNull Data data, @NotNull @Dependency("mob.entity.entity") Entity user,
            @NotNull @DataName("selector") TargetSelector<? extends LivingEntity> selector) {
        this.data = Objects.requireNonNull(data, "data");
        this.damageType = DamageType.fromEntity(Objects.requireNonNull(user, "user"));
        this.selector = Objects.requireNonNull(selector, "selector");
    }

    @Override
    public void tick(long time) {

    }

    @Override
    public void use() {
        selector.selectTarget().ifPresent(livingEntity -> livingEntity.damage(damageType, data.damage()));
    }

    @DataObject
    public record Data(@NotNull @DataPath("selector") String selectorPath, float damage) {

        public Data {
            Objects.requireNonNull(selectorPath, "selectorPath");
        }

    }

}
