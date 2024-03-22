package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Trigger;
import org.phantazm.mob2.condition.SkillCondition;
import org.phantazm.mob2.condition.SkillConditionComponent;

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
    public @NotNull Skill apply(@NotNull ExtensionHolder holder) {
        return new Internal(data, condition.apply(holder), delegate.apply(holder));
    }

    @Default("""
        {
          trigger=null
        }
        """)
    @DataObject
    public record Data(@Nullable Trigger trigger) {
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
        public void init(@NotNull Mob mob) {
            delegate.init(mob);
        }

        @Override
        public void use(@NotNull Mob mob) {
            if (condition.test(mob)) {
                delegate.use(mob);
            }
        }

        @Override
        public void tick(@NotNull Mob mob) {
            delegate.tick(mob);
        }

        @Override
        public boolean needsTicking() {
            return needsTicking;
        }

        @Override
        public void end(@NotNull Mob mob) {
            delegate.end(mob);
        }
    }
}