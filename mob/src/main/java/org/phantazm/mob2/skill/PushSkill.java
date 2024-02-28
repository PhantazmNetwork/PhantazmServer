package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.Trigger;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.util.Objects;

@Model("mob.skill.push")
@Cache
public class PushSkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent selector;

    @FactoryMethod
    public PushSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector) {
        this.data = Objects.requireNonNull(data);
        this.selector = Objects.requireNonNull(selector);
    }

    @Override
    public @NotNull Skill apply(@NotNull ExtensionHolder holder) {
        return new Internal(selector.apply(holder), data);
    }

    @Default("""
        {
          trigger=null,
          additive=false
        }
        """)
    @DataObject
    public record Data(
        @Nullable Trigger trigger,
        @NotNull @ChildPath("selector") String selector,
        double power,
        double vertical,
        boolean additive) {
    }

    private static class Internal extends TargetedSkill {
        private final Data data;

        public Internal(Selector selector, Data data) {
            super(selector);
            this.data = data;
        }

        @Override
        protected void useOnTarget(@NotNull Target target, @NotNull Mob mob) {
            target.forType(Entity.class, targetEntity -> setVelocity(mob, targetEntity));
        }

        private void setVelocity(Mob mob, Entity target) {
            Vec diff = target.getPosition().sub(mob.getPosition()).asVec().normalize();
            target.getAcquirable().sync(targetEntity -> {
                if (data.additive) {
                    targetEntity.setVelocity(
                        targetEntity.getVelocity().add(diff.mul(data.power).add(0, data.vertical, 0)));
                } else {
                    targetEntity.setVelocity(diff.mul(data.power).add(0, data.vertical, 0));
                }
            });
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }
    }
}
