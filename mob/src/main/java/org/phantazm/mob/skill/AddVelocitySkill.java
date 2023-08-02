package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.vector.Vec3D;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.VecUtils;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.target.TargetSelector;

import java.util.Objects;

@Model("mob.skill.add_velocity")
@Cache(false)
public class AddVelocitySkill implements Skill {
    private final Data data;
    private final TargetSelector<Object> selector;

    @FactoryMethod
    public AddVelocitySkill(@NotNull Data data, @NotNull @Child("selector") TargetSelector<Object> selector) {
        this.data = Objects.requireNonNull(data, "data");
        this.selector = Objects.requireNonNull(selector, "selector");
    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        selector.selectTarget(self).ifPresent(target -> {
            if (target instanceof Iterable<?> iterable) {
                for (Object object : iterable) {
                    if (object instanceof LivingEntity entity) {
                        entity.setVelocity(entity.getVelocity().add(VecUtils.toPoint(data.delta())));
                    }
                }
            }
            else if (target instanceof LivingEntity living) {
                living.setVelocity(living.getVelocity().add(VecUtils.toPoint(data.delta())));
            }
        });
    }

    @DataObject
    public record Data(@NotNull @ChildPath("selector") String selector, @NotNull Vec3D delta) {
    }
}
