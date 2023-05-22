package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.target.TargetSelector;

import java.util.Objects;

@Model("mob.skill.damage")
@Cache(false)
public class DamageEntitySkill implements Skill {

    private final Data data;
    private final TargetSelector<? extends LivingEntity> selector;

    @FactoryMethod
    public DamageEntitySkill(@NotNull Data data,
            @NotNull @Child("selector") TargetSelector<? extends LivingEntity> selector) {
        this.data = Objects.requireNonNull(data, "data");
        this.selector = Objects.requireNonNull(selector, "selector");
    }

    @Override
    public void tick(long time, @NotNull PhantazmMob self) {

    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        selector.selectTarget(self).ifPresent(
                livingEntity -> livingEntity.damage(DamageType.fromEntity(self.entity()), data.damage(),
                        data.bypassArmor));
    }

    @DataObject
    public record Data(@NotNull @ChildPath("selector") String selector, float damage, boolean bypassArmor) {

        public Data {
            Objects.requireNonNull(selector, "selector");
        }

    }

}
