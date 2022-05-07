package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.PhysicalDescriptor;
import com.github.phantazmnetwork.neuron.node.Calculator;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import com.github.phantazmnetwork.neuron.agent.Agent;
import net.minestom.server.entity.Entity;

/**
 * A {@link PhysicalDescriptor} extension designed for {@link Agent}s which are Minestom {@link Entity}s.
 */
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

    @Override
    default boolean isComplete(@NotNull Vec3I position, @NotNull Vec3I destination) {
        return position.equals(destination);
    }

    @Override
    default @NotNull Calculator getCalculator() {
        return Calculator.SQUARED_DISTANCE;
    }

    /**
     * Returns the {@link EntityType} for this descriptor. Used to provide width, depth, and height values.
     * @return the EntityType for this descriptor
     */
    @NotNull EntityType getEntityType();
}
