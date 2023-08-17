package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.ChildPath;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;

import java.util.Objects;

public class TimerSkill implements SkillComponent {
    private final Data data;
    private final Skill delegate;

    @FactoryMethod
    public TimerSkill(@NotNull Data data, @NotNull @Child("delegate") Skill delegate) {
        this.data = Objects.requireNonNull(data);
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public @NotNull Skill apply(@NotNull InjectionStore injectionStore) {
        return new Internal(delegate, data.requiresActivation, data.resetOnActivation, data.repeat, data.interval);
    }

    @DataObject
    public record Data(@NotNull @ChildPath("delegate") String delegate,
                       int repeat,
                       int interval,
                       boolean requiresActivation,
                       boolean resetOnActivation) {
        @Default("repeat")
        public static @NotNull ConfigElement defaultRepeat() {
            return ConfigPrimitive.of(-1);
        }

        @Default("requiresActivation")
        public static @NotNull ConfigElement defaultRequiresActivation() {
            return ConfigPrimitive.of(false);
        }

        @Default("resetOnActivation")
        public static @NotNull ConfigElement defaultResetOnActivation() {
            return ConfigPrimitive.of(true);
        }
    }

    private static class Internal extends TimerSkillAbstract {
        private final int interval;

        public Internal(Skill delegate, boolean requiresActivation, boolean resetOnActivation, int repeat,
                int interval) {
            super(delegate, requiresActivation, resetOnActivation, repeat, interval);
            this.interval = interval;
        }

        @Override
        public int computeInterval() {
            return interval;
        }
    }
}
