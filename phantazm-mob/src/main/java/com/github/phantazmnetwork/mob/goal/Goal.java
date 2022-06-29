package com.github.phantazmnetwork.mob.goal;

import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.NeuralGoal;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public interface Goal extends Keyed {

    @NotNull NeuralGoal createGoal(@NotNull PhantazmMob mob);

}
