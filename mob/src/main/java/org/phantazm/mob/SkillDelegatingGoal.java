package org.phantazm.mob;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.mob.skill.Skill;
import org.phantazm.proxima.bindings.minestom.goal.ProximaGoal;

import java.util.Collection;

public interface SkillDelegatingGoal extends ProximaGoal {
    @NotNull @Unmodifiable Collection<Skill> skills();
}
