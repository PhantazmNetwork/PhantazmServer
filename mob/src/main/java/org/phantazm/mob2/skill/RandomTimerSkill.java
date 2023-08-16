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
    public record Data(@ChildPath("delegate") String delegate,
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

    private static class Internal implements Skill {
        private final Data data;
        private final Skill delegate;
        private final boolean tickDelegate;
        private final Object lock = new Object();

        private boolean started;
        private int useCount;
        private int interval;
        private int activationTicks;

        private Internal(Data data, Skill delegate) {
            this.data = data;
            this.delegate = delegate;
            this.tickDelegate = delegate.needsTicking();
            this.started = !data.requiresActivation;
            this.interval = data.requiresActivation ? -1 : MathUtils.randomInterval(data.minInterval, data.maxInterval);
            this.activationTicks = -1;
        }

        @Override
        public void init() {
            delegate.init();
        }

        @Override
        public void use() {
            synchronized (lock) {
                if (data.requiresActivation) {
                    started = true;
                    interval = MathUtils.randomInterval(data.minInterval, data.maxInterval);
                }

                if (data.resetOnActivation || !started) {
                    activationTicks = -1;
                    useCount = 0;
                }
            }
        }

        @Override
        public void tick() {
            if (tickDelegate) {
                delegate.tick();
            }

            boolean useDelegate = false;
            synchronized (lock) {
                if (!started) {
                    return;
                }

                if (interval == -1L) {
                    return;
                }

                int lastUseCount = -1;
                if (data.repeat == 0 || (data.repeat > 0 && (lastUseCount = useCount) >= data.repeat)) {
                    started = false;
                    return;
                }

                if (++activationTicks >= interval) {
                    activationTicks = 0;
                    interval = MathUtils.randomInterval(data.minInterval, data.maxInterval);

                    useDelegate = true;
                    manageState(lastUseCount);
                }
            }

            if (useDelegate) {
                delegate.use();
            }
        }

        @Override
        public boolean needsTicking() {
            return true;
        }

        @Override
        public void end() {
            delegate.end();
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
