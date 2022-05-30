package com.github.phantazmnetwork.mob.goal;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.NeuralGoal;
import org.jetbrains.annotations.NotNull;

public interface GoalCreator extends VariantSerializable {

    @NotNull NeuralGoal createGoal(@NotNull PhantazmMob mob);

}
