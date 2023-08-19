package org.phantazm.mob.skill;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;

/**
 * A skill which can be used by any number of {@link PhantazmMob}s by calling {@link Skill#use(PhantazmMob)}. Skills are
 * generally either activated by specific events that act as triggers for the skill, or are activated by other skills.
 * Skills that activate other skills are known as "meta skills". Some skills need their
 * {@link Skill#tick(long, PhantazmMob)} method to be called every tick in order to implement advanced behavior. Since
 * ticking skills need to be tracked separately from non-ticking skills, the skill must opt in to being ticked by making
 * {@link Skill#needsTicking()} return {@code true}. When this method returns false, its tick method should generally
 * not be called.
 */
public interface Skill {
    /**
     * Called when a mob spawns with this skill.
     *
     * @param self the mob that spawned
     */
    default void init(@NotNull PhantazmMob self) {
    }

    /**
     * Makes the given mob use this skill.
     */
    void use(@NotNull PhantazmMob self);

    /**
     * Ticks this skill, from the perspective of the given mob.
     *
     * @param time the current system time
     * @param self the current mob
     */
    default void tick(long time, @NotNull PhantazmMob self) {
    }

    default void end(@NotNull PhantazmMob self) {
    }

    /**
     * Determines if this skill should be ticked. This value should be determined at skill construction and should not
     * change over its lifetime.
     *
     * @return {@code true} if this skill should be ticked; {@code false} otherwise
     */
    default boolean needsTicking() {
        return false;
    }
}
