package com.github.phantazmnetwork.mob.skill;

import com.github.phantazmnetwork.mob.PhantazmMob;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Skill {

    void use(@NotNull PhantazmMob sender);

}
