package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.timer.Scheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.InjectionKeys;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Trigger;

import java.util.Objects;

@Model("mob.skill.timer")
@Cache
public class TimerSkill implements SkillComponent {
    private final Data data;
    private final SkillComponent delegate;

    @FactoryMethod
    public TimerSkill(@NotNull Data data, @NotNull @Child("delegate") SkillComponent delegate) {
        this.data = Objects.requireNonNull(data);
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public @NotNull Skill apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Internal(data, delegate.apply(mob, injectionStore), mob, injectionStore.get(InjectionKeys.SCHEDULER));
    }

    @DataObject
    public record Data(@Nullable Trigger trigger,
        @NotNull @ChildPath("delegate") String delegate,
        int repeat,
        int interval,
        boolean requiresActivation,
        boolean resetOnActivation,
        boolean endImmediately) {
        @Default("trigger")
        public static @NotNull ConfigElement defaultTrigger() {
            return ConfigPrimitive.NULL;
        }

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

        @Default("endImmediately")
        public static @NotNull ConfigElement defaultEndImmediately() {
            return ConfigPrimitive.of(true);
        }
    }

    private static class Internal extends TimerSkillAbstract {
        private final Data data;
        private final int interval;

        public Internal(Data data, Skill delegate, Mob self, Scheduler scheduler) {
            super(scheduler, self, delegate, data.requiresActivation, data.resetOnActivation, data.repeat, data.interval,
                data.endImmediately);
            this.data = data;
            this.interval = data.interval;
        }

        @Override
        public int computeInterval() {
            return interval;
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }
    }
}
