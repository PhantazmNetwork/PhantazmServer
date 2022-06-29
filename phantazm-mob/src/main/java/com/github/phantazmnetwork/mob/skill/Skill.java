package com.github.phantazmnetwork.mob.skill;

import com.github.phantazmnetwork.mob.PhantazmMob;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public interface Skill extends Keyed {

    @NotNull SkillInstance createSkill(@NotNull PhantazmMob mob);

}
