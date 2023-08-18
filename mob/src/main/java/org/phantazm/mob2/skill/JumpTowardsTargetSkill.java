package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.ChildPath;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;
import org.phantazm.mob2.trigger.Trigger;

import java.util.Objects;
import java.util.Optional;

public class JumpTowardsTargetSkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent selector;

    @FactoryMethod
    public JumpTowardsTargetSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector) {
        this.data = Objects.requireNonNull(data);
        this.selector = Objects.requireNonNull(selector);
    }

    @Override
    public @NotNull Skill apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Internal(mob, selector.apply(mob, injectionStore), data);
    }

    @DataObject
    public record Data(@Nullable Trigger trigger,
                       @NotNull @ChildPath("selector") String selector,
                       double strength,
                       float angle) {
        @Default("trigger")
        public static @NotNull ConfigElement defaultTrigger() {
            return ConfigPrimitive.NULL;
        }
    }

    private static class Internal extends TargetedSkill {
        private final Data data;

        private Internal(Mob self, Selector selector, Data data) {
            super(self, selector);
            this.data = data;
        }

        @Override
        protected void useOnTarget(@NotNull Target target) {
            Optional<? extends Point> targetOptional = target.location();
            if (targetOptional.isEmpty()) {
                return;
            }

            Vec unit = Vec.fromPoint(targetOptional.get()).sub(self.getPosition()).normalize();
            Vec yeet = new Vec(unit.x(), 0, unit.z()).rotateAroundNonUnitAxis(new Vec(-unit.z(), 0, unit.x()),
                    Math.toRadians(data.angle)).mul(data.strength);

            self.getAcquirable().sync(self -> self.setVelocity(self.getVelocity().add(yeet)));
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }
    }
}
