package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.thread.Acquired;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.commons.MathUtils;
import org.phantazm.mob2.BasicMobSpawner;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Trigger;

import java.util.Objects;

@Model("mob.skill.temporal")
@Cache
public class TemporalSkill implements SkillComponent {
    private final Data data;
    private final SkillComponent delegate;

    private static class Extension {
        private int startTicks = -1;
        private int actualDelay = -1;
    }

    @FactoryMethod
    public TemporalSkill(@NotNull Data data, @NotNull @Child("delegate") SkillComponent delegate) {
        this.data = Objects.requireNonNull(data);
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public @NotNull Skill apply(@NotNull ExtensionHolder holder) {
        return new Internal(data, holder.requestKey(Extension.class), delegate.apply(holder));
    }

    @Default("""
        {
          trigger=null,
          endImmediately=false
        }
        """)
    @DataObject
    public record Data(
        @Nullable Trigger trigger,
        @NotNull @ChildPath("delegate") String delegate,
        int minDuration,
        int maxDuration,
        boolean endImmediately) {
    }

    private static class Internal implements Skill {
        private final Data data;
        private final ExtensionHolder.Key<Extension> key;
        private final Skill delegate;
        private final boolean tickDelegate;

        private Internal(Data data, ExtensionHolder.Key<Extension> key, Skill delegate) {
            this.data = data;
            this.key = key;
            this.delegate = delegate;
            this.tickDelegate = delegate.needsTicking();
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }

        @Override
        public void init(@NotNull Mob mob) {
            delegate.init(mob);
            mob.extensions().set(key, new Extension());
        }

        @Override
        public void use(@NotNull Mob mob) {
            boolean endDelegate = false;

            Extension ext = mob.extensions().get(key);
            Acquired<? extends Entity> acquired = mob.getAcquirable().lock();
            try {
                int oldStartTime = ext.startTicks;
                if (oldStartTime >= 0) {
                    if (oldStartTime < ext.actualDelay) {
                        endDelegate = true;
                    }
                }

                ext.startTicks = 0;
                ext.actualDelay = MathUtils.randomInterval(data.minDuration, data.maxDuration);
            } finally {
                acquired.unlock();
            }

            if (endDelegate) {
                delegate.end(mob);
            }

            delegate.use(mob);
        }

        @Override
        public void tick(@NotNull Mob mob) {
            if (tickDelegate) {
                delegate.tick(mob);
            }

            Extension ext = mob.extensions().get(key);
            if (ext.startTicks < 0) {
                return;
            }

            if (ext.startTicks >= ext.actualDelay) {
                delegate.end(mob);

                ext.startTicks = -1;
                ext.actualDelay = -1;
            }

            ext.startTicks++;
        }

        @Override
        public boolean needsTicking() {
            return true;
        }

        @Override
        public void end(@NotNull Mob mob) {
            boolean end = false;

            Extension ext = mob.extensions().get(key);
            Acquired<? extends Entity> acquired = mob.getAcquirable().lock();
            try {
                if (ext.startTicks < 0 || ext.actualDelay < 0) {
                    end = true;
                } else {
                    int ticksRemaining = ext.actualDelay - ext.startTicks;
                    if (ticksRemaining <= 0 || data.endImmediately) {
                        end = true;
                    } else {
                        Scheduler scheduler = mob.extensions().getOrDefault(BasicMobSpawner.SCHEDULER_KEY,
                            MinecraftServer::getSchedulerManager);
                        scheduler.scheduleTask(() -> delegate.end(mob), TaskSchedule.tick(ticksRemaining), TaskSchedule.stop(),
                            ExecutionType.SYNC);
                    }
                }
            } finally {
                acquired.unlock();
            }

            if (end) {
                delegate.end(mob);
            }
        }
    }
}
