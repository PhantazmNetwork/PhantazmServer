package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.annotation.document.Description;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
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

    private final Tag<Long> activationTicksTag;
    private final Tag<Integer> useCountTag;
    private final Tag<Boolean> startedTag;

    private final boolean tickDelegate;

    @FactoryMethod
    public TimerSkill(@NotNull Data data, @NotNull @Child("delegate") Skill delegate) {
        this.data = data;
        this.delegate = delegate;

        UUID uuid = UUID.randomUUID();
        this.activationTicksTag = Tag.Long("activation_ticks_" + uuid).defaultValue(-1L);
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
            entity.removeTag(activationTicksTag);
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

        long activationTicks = entity.getTag(activationTicksTag);
        entity.setTag(activationTicksTag, ++activationTicks);

        if (activationTicks >= data.interval) {
            entity.setTag(activationTicksTag, 0L);
            delegate.use(self);
            manageState(self, lastUseCount);
        }
    }

    @Override
    public void end(@NotNull PhantazmMob self) {
        Entity entity = self.entity();

        entity.removeTag(activationTicksTag);
        entity.removeTag(useCountTag);
        entity.removeTag(startedTag);

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
                    "infinitely") int repeat,

                       @Description("The duration of time between activations") long interval,

                       @Description("Whether the timer will start in an activated state") boolean requiresActivation,

                       @Description(
                               "Whether the timer should restart itself if it is activated while running") boolean resetOnActivation,

                       @Description("The skill to call when the timer activates") @ChildPath(
                               "delegate") String delegate) {
        @Default("requiresActivation")
        public static @NotNull ConfigElement defaultRequiresActivation() {
            return ConfigPrimitive.of(false);
        }

        @Default("resetOnActivation")
        public static @NotNull ConfigElement defaultResetOnActivation() {
            return ConfigPrimitive.of(true);
        }

        @Default("repeat")
        public static @NotNull ConfigElement defaultRepeat() {
            return ConfigPrimitive.of(-1);
        }
    }
}
