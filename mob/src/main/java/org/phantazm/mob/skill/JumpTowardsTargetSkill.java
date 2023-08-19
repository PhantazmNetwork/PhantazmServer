package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.target.TargetSelector;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;

import java.util.Optional;

@Model("mob.skill.jump_towards_target")
@Cache(false)
public class JumpTowardsTargetSkill implements Skill {
    private final Data data;
    private final TargetSelector<? extends Entity> selector;

    @FactoryMethod
    public JumpTowardsTargetSkill(@NotNull Data data,
        @NotNull @Child("selector") TargetSelector<? extends Entity> selector) {
        this.data = data;
        this.selector = selector;
    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        ProximaEntity selfEntity = self.entity();
        Optional<? extends Entity> targetOptional = selector.selectTarget(self);
        if (targetOptional.isEmpty()) {
            return;
        }

        Entity target = targetOptional.get();
        Vec targetPos = target.getPosition().asVec();
        Vec selfPos = selfEntity.getPosition().asVec();

        Vec diff = targetPos.sub(selfPos);

        double length = diff.length();
        Vec unit = diff.div(length);

        Vec yeet = new Vec(unit.x(), 0, unit.z()).rotateAroundNonUnitAxis(new Vec(-unit.z(), 0, unit.x()),
            Math.toRadians(data.angle)).mul(data.strength);

        Vec velocity = selfEntity.getVelocity();
        selfEntity.setVelocity(velocity.add(yeet));
    }

    @DataObject
    public record Data(@NotNull @ChildPath("selector") String selector,
        double strength,
        float angle) {
    }
}
