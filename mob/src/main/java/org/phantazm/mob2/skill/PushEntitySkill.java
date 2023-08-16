package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.ChildPath;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Keys;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.util.Objects;

public class PushEntitySkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent selector;

    @FactoryMethod
    public PushEntitySkill(@NotNull Data data, @NotNull SelectorComponent selector) {
        this.data = Objects.requireNonNull(data);
        this.selector = Objects.requireNonNull(selector);
    }

    @Override
    public @NotNull Skill apply(@NotNull InjectionStore injectionStore) {
        return new Internal(injectionStore.get(Keys.MOB_KEY), selector.apply(injectionStore), data);
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
                }
                else {
                    targetEntity.setVelocity(diff.mul(data.power).add(0, data.vertical, 0));
                }
            });
        }
    }
}
