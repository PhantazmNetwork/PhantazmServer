package com.github.phantazmnetwork.mob.goal;

import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.NeuralGoal;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface GoalCreator<TGoal extends NeuralGoal> {

    @NotNull TGoal createGoal(@NotNull PhantazmMob mob);

}
