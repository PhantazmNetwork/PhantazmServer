package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.ChildPath;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;

import java.util.Random;

public class RandomSkill implements SkillComponent {
    private final Data data;
    private final Random random;
    private final SkillComponent delegate;

    @FactoryMethod
    public RandomSkill(@NotNull Data data, @NotNull @Child("delegate") SkillComponent delegate) {
        this.data = data;
        this.random = new Random();
        this.delegate = delegate;
    }

    @Override
    public @NotNull Skill apply(@NotNull InjectionStore injectionStore) {
        return new Internal(data, delegate.apply(injectionStore), random);
    }

    @DataObject
    public record Data(@NotNull @ChildPath("delegate") String delegate, double chance) {
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
