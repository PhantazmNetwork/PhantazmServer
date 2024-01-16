package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Scheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.commons.MathUtils;
import org.phantazm.mob2.InjectionKeys;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Trigger;

import java.util.Objects;

@Model("mob.skill.random_timer")
@Cache
public class RandomTimerSkill implements SkillComponent {
    private final Data data;
    private final SkillComponent delegate;

    @FactoryMethod
    public RandomTimerSkill(@NotNull Data data, @NotNull @Child("delegate") SkillComponent delegate) {
        this.data = Objects.requireNonNull(data);
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public @NotNull Skill apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Internal(data, delegate.apply(mob, injectionStore), mob, injectionStore
            .getOrDefault(InjectionKeys.SCHEDULER, MinecraftServer::getSchedulerManager));
    }

    @DataObject
    public record Data(@Nullable Trigger trigger,
        @NotNull @ChildPath("delegate") String delegate,
        int repeat,
        int minInterval,
        int maxInterval,
        boolean requiresActivation,
        boolean resetOnActivation,
        boolean endImmediately) {
        @Default("trigger")
        public static @NotNull ConfigElement defaultTrigger() {
            return ConfigPrimitive.NULL;
        }

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

        @Default("endImmediately")
        public static @NotNull ConfigElement defaultEndImmediately() {
            return ConfigPrimitive.of(true);
        }
    }

    private static class Internal extends TimerSkillAbstract {
        private final Data data;

        public Internal(Data data, Skill delegate, Mob self, Scheduler scheduler) {
            super(scheduler, self, delegate, data.requiresActivation, data.resetOnActivation, data.repeat,
                MathUtils.randomInterval(data.minInterval, data.maxInterval), data.endImmediately);
            this.data = data;
        }

        @Override
        public int computeInterval() {
            return MathUtils.randomInterval(data.minInterval, data.maxInterval);
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }
    }
}
