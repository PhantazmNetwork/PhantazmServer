package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.Trigger;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.util.Objects;

@Model("mob.skill.push_entity")
@Cache
public class PushEntitySkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent selector;

    @FactoryMethod
    public PushEntitySkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector) {
        this.data = Objects.requireNonNull(data);
        this.selector = Objects.requireNonNull(selector);
    }

    @Override
    public @NotNull Skill apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Internal(mob, selector.apply(mob, injectionStore), data);
    }

    @DataObject
    public record Data(
        @Nullable Trigger trigger,
        @NotNull @ChildPath("selector") String selector,
        double power,
        double vertical,
        boolean additive) {
        @Default("trigger")
        public static @NotNull ConfigElement defaultTrigger() {
            return ConfigPrimitive.NULL;
        }

        @Default("additive")
        public static @NotNull ConfigElement defaultAdditive() {
            return ConfigPrimitive.of(false);
        }
    }

    private static class Internal extends TargetedSkill {
        private final Data data;

        public Internal(Mob self, Selector selector, Data data) {
            super(self, selector);
            this.data = data;
        }

        @Override
        protected void useOnTarget(@NotNull Target target) {
            target.forType(Entity.class, this::setVelocity);
        }

        private void setVelocity(Entity target) {
            Vec diff = target.getPosition().sub(self.getPosition()).asVec().normalize();
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
