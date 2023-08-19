package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.target.TargetSelector;

import java.util.Objects;

@Model("mob.skill.push")
@Cache(false)
public class PushEntitySkill implements Skill {
    private final Data data;
    private final TargetSelector<Object> selector;

    @FactoryMethod
    public PushEntitySkill(@NotNull Data data, @NotNull @Child("selector") TargetSelector<Object> selector) {
        this.data = Objects.requireNonNull(data);
        this.selector = Objects.requireNonNull(selector);
    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        selector.selectTarget(self).ifPresent(livingEntity -> {
            if (livingEntity instanceof Iterable<?> iterable) {
                for (Object object : iterable) {
                    if (object instanceof Entity entity) {
                        setVelocity(entity, self.entity());
                    }
                }
            } else if (livingEntity instanceof LivingEntity living) {
                setVelocity(living, self.entity());
            }
        });
    }

    private void setVelocity(Entity target, Entity self) {
        Vec diff = target.getPosition().sub(self.getPosition()).asVec().normalize();

        if (data.additive) {
            target.setVelocity(target.getVelocity().add(diff.mul(data.power).add(0, data.vertical, 0)));
        } else {
            target.setVelocity(diff.mul(data.power).add(0, data.vertical, 0));
        }
    }

    @DataObject
    public record Data(@NotNull @ChildPath("selector") String selector,
        double power,
        double vertical,
        boolean additive) {
        @Default("additive")
        public static @NotNull ConfigElement defaultAdditive() {
            return ConfigPrimitive.of(false);
        }
    }
}
