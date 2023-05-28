package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.annotation.document.Description;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;

import java.util.UUID;

@Description("""
        A timed meta skill that can activate another skill periodically after a delay, a set number of times, or
        infinitely. Time is measured from the moment that the mob spawned. The timer can be set to start as soon as the
        mob spawns, or it can start when this skill is activated. If a timer is activated while it is already counting
        down, it can be configured to restart.
        """)
@Model("mob.skill.timer")
@Cache(false)
public class TimerSkill implements Skill {
    private final Data data;
    private final Skill delegate;

    private final Tag<Long> lastActivationTag;
    private final Tag<Integer> useCountTag;
    private final Tag<Boolean> startedTag;

    private final boolean tickDelegate;

    @FactoryMethod
    public TimerSkill(@NotNull Data data, @NotNull @Child("delegate") Skill delegate) {
        this.data = data;
        this.delegate = delegate;

        UUID uuid = UUID.randomUUID();
        this.lastActivationTag = Tag.Long("last_activation_" + uuid).defaultValue(-1L);
        this.useCountTag = Tag.Integer("use_count_" + uuid).defaultValue(0);
        this.startedTag = Tag.Boolean("started_" + uuid).defaultValue(!data.requiresActivation);
        this.tickDelegate = delegate.needsTicking();
    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        Entity entity = self.entity();
        if (data.requiresActivation) {
            entity.setTag(startedTag, true);
        }

        if (data.resetOnActivation || !entity.getTag(startedTag)) {
            entity.removeTag(lastActivationTag);
            entity.removeTag(useCountTag);
        }
    }

    @Override
    public void tick(long time, @NotNull PhantazmMob self) {
        if (tickDelegate) {
            delegate.tick(time, self);
        }

        Entity entity = self.entity();
        if (!entity.getTag(startedTag)) {
            return;
        }

        int lastUseCount = -1;
        if (data.repeat == 0 || (data.repeat > 0 && (lastUseCount = entity.getTag(useCountTag)) >= data.repeat)) {
            entity.setTag(startedTag, false);
            return;
        }

        long lastActivationTime = entity.getTag(lastActivationTag);
        if (lastActivationTime == -1) {
            entity.setTag(lastActivationTag, lastActivationTime = time);
        }

        if ((time - lastActivationTime) / MinecraftServer.TICK_MS >= data.interval) {
            entity.setTag(lastActivationTag, time);
            delegate.use(self);
            manageState(entity, lastUseCount);
        }
    }

    private void manageState(Entity entity, int lastUseCount) {
        if (lastUseCount != -1) {
            entity.setTag(useCountTag, ++lastUseCount);

            if (lastUseCount >= data.repeat) {
                entity.setTag(startedTag, false);
            }
        }
    }

    @Override
    public boolean needsTicking() {
        return true;
    }

    @DataObject
    public record Data(@Description(
            "The number of times this skill should repeat its action. A negative number will repeat " +
                    "infinitely") int repeat,

                       @Description("The duration of time between activations") long interval,

                       @Description("Whether the timer will start in an activated state") boolean requiresActivation,

                       @Description(
                               "Whether the timer should restart itself if it is activated while running") boolean resetOnActivation,

                       @Description("The skill to call when the timer activates") @ChildPath(
                               "delegate") String delegate) {
    }
}
