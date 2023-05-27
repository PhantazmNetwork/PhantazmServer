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
        infinitely. Time can either be measured on an "absolute" basis (in which case all entities using the skill
        share the same timer), or from the moment that the mob spawned.
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

    private long lastActivation = -1;

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
        if (data.requiresActivation) {
            self.entity().setTag(startedTag, true);
        }

        if (data.fromSpawn) {
            self.entity().removeTag(lastActivationTag);
            self.entity().removeTag(useCountTag);
        }
        else {
            lastActivation = -1;
        }
    }

    @Override
    public void tick(long time, @NotNull PhantazmMob self) {
        if (tickDelegate) {
            delegate.tick(time, self);
        }

        if (!self.entity().getTag(startedTag)) {
            return;
        }

        int lastUseCount = -1;
        if (data.repeat == 0 ||
                (data.repeat > 0 && (lastUseCount = self.entity().getTag(useCountTag)) >= data.repeat)) {
            if (data.requiresActivation) {
                self.entity().setTag(startedTag, false);
            }

            return;
        }

        if (data.fromSpawn) {
            Entity entity = self.entity();
            long lastActivationTime = entity.getTag(lastActivationTag);
            if (lastActivationTime == -1) {
                entity.setTag(lastActivationTag, lastActivationTime = time);
            }

            if ((time - lastActivationTime) / MinecraftServer.TICK_MS >= data.interval) {
                entity.setTag(lastActivationTag, time);
                delegate.use(self);
                if (lastUseCount != -1) {
                    self.entity().setTag(useCountTag, lastUseCount + 1);
                }
            }

            return;
        }

        if (lastActivation == -1) {
            lastActivation = time;
        }

        if ((time - lastActivation) / MinecraftServer.TICK_MS >= data.interval) {
            lastActivation = time;
            delegate.use(self);
            if (lastUseCount != -1) {
                self.entity().setTag(useCountTag, lastUseCount + 1);
            }
        }
    }

    @Override
    public boolean needsTicking() {
        return true;
    }

    @DataObject
    public record Data(@Description("The number of times this skill should repeat its action. A negative number " +
            "will repeat infinitely") int repeat,
                       @Description("The duration of time between activations") long interval,
                       @Description("Whether to perform timing from entity spawn or globally") boolean fromSpawn,
                       @Description(
                               "Whether to start the timer immediately, or only on activation") boolean requiresActivation,
                       @Description("The skill to call when the timer activates") @ChildPath(
                               "delegate") String delegate) {
    }
}
