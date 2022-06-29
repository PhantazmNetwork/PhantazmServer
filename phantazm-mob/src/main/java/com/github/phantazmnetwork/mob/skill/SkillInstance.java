package com.github.phantazmnetwork.mob.skill;

import com.github.phantazmnetwork.mob.PhantazmMob;

/**
 * A behavior that can be executed.
 * The definition is very broad and is typically used by {@link PhantazmMob}s.
 */
@FunctionalInterface
public interface SkillInstance {

    /**
     * Uses the skill.
     */
    void use();

}
