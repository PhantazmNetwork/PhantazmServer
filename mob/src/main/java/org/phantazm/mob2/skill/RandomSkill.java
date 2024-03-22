package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Trigger;

import java.util.Objects;
import java.util.Random;

@Model("mob.skill.random")
@Cache
public class RandomSkill implements SkillComponent {
    private final Data data;
    private final Random random;
    private final SkillComponent delegate;

    @FactoryMethod
    public RandomSkill(@NotNull Data data, @NotNull @Child("delegate") SkillComponent delegate) {
        this.data = Objects.requireNonNull(data);
        this.random = new Random();
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public @NotNull Skill apply(@NotNull ExtensionHolder holder) {
        return new Internal(data, delegate.apply(holder), random);
    }

    @Default("""
        {
          trigger=null
        }
        """)
    @DataObject
    public record Data(@Nullable Trigger trigger,
        double chance) {
    }

    private static class Internal implements Skill {
        private final Data data;
        private final Skill delegate;
        private final Random random;
        private final boolean needsTicking;

        private Internal(Data data, Skill delegate, Random random) {
            this.data = data;
            this.delegate = delegate;
            this.random = random;
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
            if (data.chance <= 0) {
                return;
            }

            if (data.chance >= 1 || random.nextDouble() < data.chance) {
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
