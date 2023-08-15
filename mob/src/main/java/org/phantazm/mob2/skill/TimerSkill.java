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

public class TimerSkill implements SkillComponent {
    private final Data data;
    private final Skill delegate;

    @FactoryMethod
    public TimerSkill(@NotNull Data data, @NotNull @Child("delegate") Skill delegate) {
        this.data = data;
        this.delegate = delegate;
    }

    @Override
    public @NotNull Skill apply(@NotNull InjectionStore injectionStore) {
        return new Internal(data, delegate);
    }

    @DataObject
    public record Data(int repeat,
                       long interval,
                       boolean requiresActivation,
                       boolean resetOnActivation,
                       @NotNull @ChildPath("delegate") String delegate) {
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

    private static class Internal implements Skill {
        private final Data data;
        private final Skill delegate;
        private final boolean tickDelegate;

        private boolean started;
        private long activationTicks;
        private int useCount;

        private Internal(Data data, Skill delegate) {
            this.data = data;
            this.delegate = delegate;
            this.tickDelegate = delegate.needsTicking();
            this.started = !data.requiresActivation;
        }

        @Override
        public void use() {
            if (data.requiresActivation) {
                started = true;
            }

            if (data.resetOnActivation || !started) {
                activationTicks = -1;
                useCount = 0;
            }
        }

        @Override
        public void tick() {
            if (tickDelegate) {
                delegate.tick();
            }

            if (!started) {
                return;
            }

            int lastUseCount = -1;
            if (data.repeat == 0 || (data.repeat > 0 && (lastUseCount = useCount) >= data.repeat)) {
                started = false;
                return;
            }

            ++activationTicks;
            if (activationTicks >= data.interval) {
                activationTicks = 0;
                delegate.use();
                manageState(lastUseCount);
            }
        }

        @Override
        public void end() {
            activationTicks = -1;
            useCount = 0;
            started = false;
            delegate.end();
        }

        @Override
        public boolean needsTicking() {
            return true;
        }

        private void manageState(int lastUseCount) {
            if (lastUseCount == -1) {
                return;
            }

            useCount = ++lastUseCount;
            if (lastUseCount >= data.repeat) {
                started = false;
            }
        }
    }
}
