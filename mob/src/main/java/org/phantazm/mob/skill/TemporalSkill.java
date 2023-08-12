package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.MathUtils;
import org.phantazm.mob.PhantazmMob;

import java.util.UUID;

@Model("mob.skill.temporal")
@Cache(false)
public class TemporalSkill implements Skill {
    private final Data data;
    private final Skill delegate;

    private final boolean delegateNeedsTicking;
    private final Tag<Long> startTicks;
    private final Tag<Long> actualDelay;

    @FactoryMethod
    public TemporalSkill(@NotNull Data data, @NotNull @Child("delegate") Skill delegate) {
        this.data = data;
        this.delegate = delegate;
        this.delegateNeedsTicking = delegate.needsTicking();

        UUID uuid = UUID.randomUUID();
        this.startTicks = Tag.Long("start_ticks_" + uuid).defaultValue(-1L);
        this.actualDelay = Tag.Long("actual_delay_" + uuid).defaultValue(-1L);
    }

    @Override
    public void tick(long time, @NotNull PhantazmMob self) {
        if (delegateNeedsTicking) {
            delegate.tick(time, self);
        }

        Entity entity = self.entity();
        long ticks = entity.getTag(this.startTicks);
        if (ticks < 0) {
            return;
        }

        if (ticks >= entity.getTag(this.actualDelay)) {
            delegate.end(self);

            entity.removeTag(this.startTicks);
            entity.removeTag(actualDelay);
        }

        entity.setTag(this.startTicks, ticks + 1);
    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        Entity entity = self.entity();
        long oldStartTime = entity.getTag(this.startTicks);
        if (oldStartTime >= 0) {
            if (oldStartTime < entity.getTag(this.actualDelay)) {
                delegate.end(self);
            }
        }

        delegate.use(self);

        entity.setTag(startTicks, 0L);
        entity.setTag(actualDelay, MathUtils.randomInterval(data.minDuration, data.maxDuration));
    }

    @Override
    public void end(@NotNull PhantazmMob self) {
        Entity entity = self.entity();
        long ticks = entity.getTag(this.startTicks);
        long actualDelay = entity.getTag(this.actualDelay);
        if (ticks < 0 || actualDelay < 0) {
            delegate.end(self);
            return;
        }

        long ticksRemaining = actualDelay - ticks;
        if (ticksRemaining <= 0) {
            delegate.end(self);
            return;
        }

        MinecraftServer.getSchedulerManager()
                .scheduleTask(() -> delegate.end(self), TaskSchedule.tick((int)ticksRemaining), TaskSchedule.stop(),
                        ExecutionType.SYNC);
    }

    @Override
    public boolean needsTicking() {
        return true;
    }

    @DataObject
    public record Data(long minDuration, int maxDuration, @NotNull @ChildPath("delegate") String delegate) {
    }
}
