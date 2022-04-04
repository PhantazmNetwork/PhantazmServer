package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.neuron.agent.GroundDescriptor;

public interface GroundMinestomDescriptor extends MinestomDescriptor, GroundDescriptor {
    @Override
    default float getJumpHeight() {
        return 1F;
    }

    @Override
    default float getFallTolerance() {
        return 16F;
    }
}
