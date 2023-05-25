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
@Model("mob.skill.timed")
@Cache(false)
public class TimedSkill implements Skill {
    private final Data data;
    private final Skill delegate;

    private final Tag<Long> lastActivationTag;
    private final Tag<Integer> useCountTag;

    private long lastActivation = -1;

    @FactoryMethod
    public TimedSkill(@NotNull Data data, @NotNull @Child("delegate") Skill delegate) {
        this.data = data;
        this.delegate = delegate;

        UUID uuid = UUID.randomUUID();
        this.lastActivationTag = data.fromSpawn ? Tag.Long("last_activation_" + uuid).defaultValue(-1L) : null;
        this.useCountTag = data.repeat < 1 ? null : Tag.Integer("use_count_" + uuid);
    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        //no-op, this skill activates its delegate only on timer completion
    }

    @Override
    public void tick(long time, @NotNull PhantazmMob self) {
        int lastUseCount = -1;
        if (data.repeat == 0 ||
                (data.repeat > 0 && (lastUseCount = self.entity().getTag(useCountTag)) >= data.repeat)) {
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
                       @Description("The skill to call when the timer activates") @ChildPath(
                               "delegate") String delegate) {
    }
}
