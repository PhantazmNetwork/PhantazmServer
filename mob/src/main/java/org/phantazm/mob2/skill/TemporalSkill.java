package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.ChildPath;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.thread.Acquired;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.commons.MathUtils;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Trigger;

import java.util.Objects;

public class TemporalSkill implements SkillComponent {
    private final Data data;
    private final SkillComponent delegate;

    @FactoryMethod
    public TemporalSkill(@NotNull Data data, @NotNull @Child("delegate") SkillComponent delegate) {
        this.data = Objects.requireNonNull(data);
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public @NotNull Skill apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Internal(data, delegate.apply(mob, injectionStore), mob);
    }

    @DataObject
    public record Data(
            @Nullable Trigger trigger,
            @NotNull @ChildPath("delegate") String delegate,
            int minDuration,
            int maxDuration) {
        @Default("trigger")
        public static @NotNull ConfigElement defaultTrigger() {
            return ConfigPrimitive.NULL;
        }
    }

    private static class Internal implements Skill {
        private final Data data;
        private final Skill delegate;
        private final boolean tickDelegate;
        private final Mob self;

        private int startTicks;
        private int actualDelay;

        private Internal(Data data, Skill delegate, Mob self) {
            this.data = data;
            this.delegate = delegate;
            this.tickDelegate = delegate.needsTicking();
            this.self = self;
            this.startTicks = -1;
            this.actualDelay = -1;
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
            boolean endDelegate = false;

            Acquired<Entity> acquired = self.getAcquirable().lock();
            try {
                int oldStartTime = this.startTicks;
                if (oldStartTime >= 0) {
                    if (oldStartTime < actualDelay) {
                        endDelegate = true;
                    }
                }

                startTicks = 0;
                actualDelay = MathUtils.randomInterval(data.minDuration, data.maxDuration);
            } finally {
                acquired.unlock();
            }

            if (endDelegate) {
                delegate.end();
            }

            delegate.use();
        }

        @Override
        public void tick() {
            if (tickDelegate) {
                delegate.tick();
            }

            if (startTicks < 0) {
                return;
            }

            if (startTicks >= actualDelay) {
                delegate.end();

                startTicks = -1;
                actualDelay = -1;
            }

            startTicks++;
        }

        @Override
        public boolean needsTicking() {
            return true;
        }

        @Override
        public void end() {
            boolean end = false;

            Acquired<Entity> acquired = self.getAcquirable().lock();
            try {
                if (this.startTicks < 0 || this.actualDelay < 0) {
                    end = true;
                } else {
                    int ticksRemaining = this.actualDelay - this.startTicks;
                    if (ticksRemaining <= 0) {
                        end = true;
                    } else {
                        MinecraftServer.getSchedulerManager()
                                .scheduleTask(delegate::end, TaskSchedule.tick(ticksRemaining), TaskSchedule.stop(),
                                        ExecutionType.SYNC);
                    }
                }
            } finally {
                acquired.unlock();
            }

            if (end) {
                delegate.end();
            }
        }
    }
}
