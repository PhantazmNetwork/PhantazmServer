package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.ChildPath;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Trigger;

import java.util.Objects;
import java.util.Random;

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
    public @NotNull Skill apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Internal(data, delegate.apply(mob, injectionStore), random);
    }

    @DataObject
    public record Data(@Nullable Trigger trigger, @NotNull @ChildPath("delegate") String delegate, double chance) {
        @Default("trigger")
        public static @NotNull ConfigElement defaultTrigger() {
            return ConfigPrimitive.NULL;
        }
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
        public void init() {
            delegate.init();
        }

        @Override
        public void use() {
            if (data.chance <= 0) {
                return;
            }

            if (data.chance >= 1 || random.nextDouble() < data.chance) {
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
