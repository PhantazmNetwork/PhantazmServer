package com.github.phantazmnetwork.mob.skill;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.mob.PhantazmMob;
import org.jetbrains.annotations.NotNull;

/**
 * Creates {@link SkillInstance}s from an associated {@link PhantazmMob}.
 */
public interface Skill extends VariantSerializable {

    /**
     * Creates a new {@link SkillInstance}.
     * @param mob The {@link Skill}'s user
     * @return A new {@link SkillInstance}
     */
    @NotNull SkillInstance createSkill(@NotNull PhantazmMob mob);

}
