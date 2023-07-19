package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.annotation.document.Description;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.MathUtils;
import org.phantazm.mob.PhantazmMob;

import java.util.UUID;

@Description("""
        A timed meta skill that can activate another skill periodically after a delay, a set number of times, or
        infinitely. Time is measured from the moment that the mob spawned. The timer can be set to start as soon as the
        mob spawns, or it can start when this skill is activated. If a timer is activated while it is already counting
        down, it can be configured to restart.
        """)
@Model("mob.skill.timer_random")
@Cache(false)
public class RandomTimerSkill implements Skill {
    private final Data data;
    private final Skill delegate;

    private final Tag<Long> lastActivationTag;
    private final Tag<Integer> useCountTag;
    private final Tag<Boolean> startedTag;
    private final Tag<Long> intervalTag;

    private final boolean tickDelegate;

    @FactoryMethod
    public RandomTimerSkill(@NotNull Data data, @NotNull @Child("delegate") Skill delegate) {
        this.data = data;
        this.delegate = delegate;

        UUID uuid = UUID.randomUUID();
        this.lastActivationTag = Tag.Long("last_activation_" + uuid).defaultValue(-1L);
        this.useCountTag = Tag.Integer("use_count_" + uuid).defaultValue(0);
        this.startedTag = Tag.Boolean("started_" + uuid).defaultValue(!data.requiresActivation);
        this.intervalTag = Tag.Long("interval_" + uuid).defaultValue(
                data.requiresActivation ? -1L : MathUtils.randomInterval(data.minInterval, data.maxInterval));
        this.tickDelegate = delegate.needsTicking();
    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        Entity entity = self.entity();
        if (data.requiresActivation) {
            entity.setTag(startedTag, true);
            entity.setTag(intervalTag, MathUtils.randomInterval(data.minInterval, data.maxInterval));
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

        long actualInterval = entity.getTag(intervalTag);
        if (actualInterval == -1L) {
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

        if ((time - lastActivationTime) / MinecraftServer.TICK_MS >= actualInterval) {
            entity.setTag(lastActivationTag, time);
            entity.setTag(intervalTag, MathUtils.randomInterval(data.minInterval, data.maxInterval));

            delegate.use(self);
            manageState(self, lastUseCount);
        }
    }

    @Override
    public void end(@NotNull PhantazmMob self) {
        Entity entity = self.entity();

        entity.removeTag(lastActivationTag);
        entity.removeTag(useCountTag);
        entity.removeTag(startedTag);
        entity.removeTag(intervalTag);

        delegate.end(self);
    }

    private void manageState(PhantazmMob self, int lastUseCount) {
        if (lastUseCount != -1) {
            Entity entity = self.entity();
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
                    "infinitely. Defaults to -1.") int repeat,

                       @Description("The minimum activation interval") long minInterval,
                       @Description("The maximum activation interval") long maxInterval,

                       @Description(
                               "Whether the timer will start in an activated state. Defaults to false.") boolean requiresActivation,

                       @Description(
                               "Whether the timer should restart itself if it is activated while running. Defaults to false.") boolean resetOnActivation,

                       @Description("The skill to call when the timer activates") @ChildPath(
                               "delegate") String delegate) {
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
}
