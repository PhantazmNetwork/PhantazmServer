package com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal;

import com.github.phantazmnetwork.commons.Tickable;

public interface NeuralGoal extends Tickable {

    boolean shouldStart();

    void start();

    boolean shouldEnd();

    void end();

}
