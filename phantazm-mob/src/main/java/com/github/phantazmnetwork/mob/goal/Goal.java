package com.github.phantazmnetwork.mob.goal;

import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.NeuralGoal;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

/**
 * Creates {@link NeuralGoal}s from an associated {@link PhantazmMob}.
 */
public interface Goal extends Keyed {

    /**
     * Creates a new {@link NeuralGoal}.
     * @param mob The {@link Goal}'s user
     * @return A new {@link NeuralGoal}
     */
    @NotNull NeuralGoal createGoal(@NotNull PhantazmMob mob);

}
