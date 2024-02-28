package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.Trigger;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.util.Objects;
import java.util.Optional;

@Model("mob.skill.leap")
@Cache
public class LeapSkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent selector;

    @FactoryMethod
    public LeapSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector) {
        this.data = Objects.requireNonNull(data);
        this.selector = Objects.requireNonNull(selector);
    }

    @Override
    public @NotNull Skill apply(@NotNull ExtensionHolder holder) {
        return new Internal(selector.apply(holder), data);
    }

    @Default("""
        {
          trigger=null
        }
        """)
    @DataObject
    public record Data(
        @Nullable Trigger trigger,
        @NotNull @ChildPath("selector") String selector,
        double strength,
        float angle) {
    }

    private static class Internal extends TargetedSkill {
        private final Data data;

        private Internal(Selector selector, Data data) {
            super(selector);
            this.data = data;
        }

        @Override
        protected void useOnTarget(@NotNull Target target, @NotNull Mob mob) {
            Optional<? extends Point> targetOptional = target.location();
            if (targetOptional.isEmpty()) {
                return;
            }

            Vec unit = Vec.fromPoint(targetOptional.get()).sub(mob.getPosition()).normalize();
            Vec yeet = new Vec(unit.x(), 0, unit.z()).rotateAroundNonUnitAxis(new Vec(-unit.z(), 0, unit.x()),
                Math.toRadians(data.angle)).mul(data.strength);

            mob.getAcquirable().sync(self -> self.setVelocity(self.getVelocity().add(yeet)));
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }
    }
}
