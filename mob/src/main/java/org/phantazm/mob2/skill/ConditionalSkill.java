package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Trigger;

@Model("mob.skill.conditional")
@Cache
public class ConditionalSkill implements SkillComponent {
    private final Data data;
    private final SkillConditionComponent condition;
    private final SkillComponent delegate;

    @FactoryMethod
    public ConditionalSkill(@NotNull Data data, @NotNull @Child("condition") SkillConditionComponent condition,
        @NotNull @Child("delegate") SkillComponent delegate) {
        this.data = data;
        this.condition = condition;
        this.delegate = delegate;
    }

    @Override
    public @NotNull Skill apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Internal(data, condition.apply(mob, injectionStore), delegate.apply(mob, injectionStore));
    }

    @Default("""
        {
          trigger=null
        }
        """)
    @DataObject
    public record Data(@Nullable Trigger trigger,
        @NotNull @ChildPath("condition") String condition,
        @NotNull @ChildPath("delegate") String delegate) {
    }

    private static class Internal implements Skill {
        private final Data data;
        private final SkillCondition condition;
        private final Skill delegate;

        private final boolean needsTicking;

        private Internal(Data data, SkillCondition condition, Skill delegate) {
            this.data = data;
            this.condition = condition;
            this.delegate = delegate;

            this.needsTicking = delegate.needsTicking();
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }

        @Override
        public void init() {
            delegate.init();
        }

        @Override
        public void use() {
            if (condition.test()) {
                delegate.use();
            }
        }

        @Override
        public void tick() {
            delegate.tick();
        }

        @Override
        public boolean needsTicking() {
            return needsTicking;
        }

        @Override
        public void end() {
            delegate.end();
        }
    }
}