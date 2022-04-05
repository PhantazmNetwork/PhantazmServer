package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.neuron.agent.PhysicalDescriptor;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public interface MinestomDescriptor extends PhysicalDescriptor {
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

    @NotNull EntityType getEntityType();
}
