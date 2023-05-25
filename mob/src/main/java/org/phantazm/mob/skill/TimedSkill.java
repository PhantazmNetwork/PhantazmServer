package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.annotation.document.Description;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;

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

    @FactoryMethod
    public TimedSkill(@NotNull Data data, @NotNull @Child("delegate") Skill delegate) {
        this.data = data;
        this.delegate = delegate;
    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        //no-op, this skill activates its delegate only on timer completion
    }

    @Override
    public void tick(long time, @NotNull PhantazmMob self) {

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
