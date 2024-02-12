package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.Trigger;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.util.Objects;

@Model("mob.skill.heal")
@Cache
public class HealSkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent selector;

    @FactoryMethod
    public HealSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector) {
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
        float amount) {
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
            target.forType(LivingEntity.class, livingEntity -> livingEntity.getAcquirable().sync(e -> {
                LivingEntity entity = (LivingEntity) e;
                entity.setHealth(entity.getHealth() + data.amount);
            }));
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }
    }
}
