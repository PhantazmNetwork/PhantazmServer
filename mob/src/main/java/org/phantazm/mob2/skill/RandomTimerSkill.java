package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.ChildPath;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.commons.MathUtils;

import java.util.Objects;

public class RandomTimerSkill implements SkillComponent {
    private final Data data;
    private final SkillComponent delegate;

    @FactoryMethod
    public RandomTimerSkill(@NotNull Data data, @NotNull SkillComponent delegate) {
        this.data = Objects.requireNonNull(data);
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public @NotNull Skill apply(@NotNull InjectionStore injectionStore) {
        return new Internal(data, delegate.apply(injectionStore));
    }

    @DataObject
    public record Data(@NotNull @ChildPath("delegate") String delegate,
                       int repeat,
                       int minInterval,
                       int maxInterval,
                       boolean requiresActivation,
                       boolean resetOnActivation) {
        @Default("repeat")
        public static @NotNull ConfigElement repeatDefault() {
            return ConfigPrimitive.of(-1);
        }

        @Default("requiresActivation")
        public static @NotNull ConfigElement requiresActivationDefault() {
            return ConfigPrimitive.of(false);
        }

        @Default("resetOnActivation")
        public static @NotNull ConfigElement resetOnActivationDefault() {
            return ConfigPrimitive.of(false);
        }
    }

    private static class Internal extends TimerSkillAbstract {
        private final Data data;

        public Internal(Data data, Skill delegate) {
            super(delegate, data.requiresActivation, data.resetOnActivation, data.repeat,
                    computeInterval0(data.minInterval, data.maxInterval));
            this.data = data;
        }

        @Override
        public int computeInterval() {
            return computeInterval0(data.minInterval, data.maxInterval);
        }

        private static int computeInterval0(int min, int max) {
            return MathUtils.randomInterval(min, max);
        }
    }
}
