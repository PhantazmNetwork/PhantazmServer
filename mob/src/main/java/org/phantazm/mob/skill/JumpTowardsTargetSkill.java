package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;

@Model("mob.skill.jump_towards_target")
@Cache(false)
public class JumpTowardsTargetSkill implements Skill {
    private final Data data;

    @FactoryMethod
    public JumpTowardsTargetSkill(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        ProximaEntity selfEntity = self.entity();
        Entity target = selfEntity.getTargetEntity();
        if (target != null) {
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
    }

    @DataObject
    public record Data(double strength, float angle) {
    }
}
