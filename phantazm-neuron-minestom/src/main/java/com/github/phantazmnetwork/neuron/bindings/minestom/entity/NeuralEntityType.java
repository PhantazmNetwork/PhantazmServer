package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.neuron.agent.GroundDescriptor;
import com.github.phantazmnetwork.neuron.agent.PhysicalDescriptor;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public interface NeuralEntityType extends PhysicalDescriptor {
    @NotNull EntityType getEntityType();

    float getSpeed();

    @Override
    default float getWidth() {
        return (float) getEntityType().width();
    }

    @Override
    default float getDepth() {
        return getWidth();
    }

    @Override
    default float getHeight() {
        return (float) getEntityType().height();
    }
}
