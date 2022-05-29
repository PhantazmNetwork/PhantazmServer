package com.github.phantazmnetwork.mob.goal;

import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.GoalGroup;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface GoalGroupCreator {

    @NotNull GoalGroup createGoalGroup(@NotNull PhantazmMob mob);

}
