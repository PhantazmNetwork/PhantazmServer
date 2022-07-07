package com.github.phantazmnetwork.mob.skill;

import net.kyori.adventure.key.Keyed;
import com.github.phantazmnetwork.mob.PhantazmMob;
import org.jetbrains.annotations.NotNull;

/**
 * Creates {@link SkillInstance}s from an associated {@link PhantazmMob}.
 */
public interface Skill extends Keyed {

    /**
     * Creates a new {@link SkillInstance}.
     * @param mob The {@link Skill}'s user
     * @return A new {@link SkillInstance}
     */
    @NotNull SkillInstance createSkill(@NotNull PhantazmMob mob);

}
