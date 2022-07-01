package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.agent.PhysicalDescriptor;
import com.github.phantazmnetwork.neuron.engine.PathContext;
import com.github.phantazmnetwork.neuron.navigator.Controller;
import com.github.phantazmnetwork.neuron.navigator.Navigator;
import com.github.phantazmnetwork.neuron.node.Calculator;
import com.github.phantazmnetwork.neuron.node.NodeTranslator;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link PhysicalDescriptor} extension designed for {@link Agent}s which are Minestom {@link Entity}s.
 */
public interface MinestomDescriptor extends PhysicalDescriptor, VariantSerializable {
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

    /**
     * Computes the block position (destination node vector) for the given entity.
     * @param targetEntity the entity to compute the target position for
     * @return the destination node vector
     */
    @NotNull Vec3I computeTargetPosition(@NotNull Entity targetEntity);

    /**
     * Determines if the given entity is valid for pathfinding.
     * @param entity the entity
     * @return {@code true} if the entity is valid for pathfinding, {@code false} otherwise
     */
    boolean canPathfind(@NotNull NeuralEntity entity);

    /**
     * Creates a {@link Controller} suitable for making the given entity move along a path.
     * @param entity the entity to make a controller for
     * @return a Controller suitable for the given entity's movement
     */
    @NotNull Controller makeController(@NotNull NeuralEntity entity);

    /**
     * Creates a {@link NodeTranslator} instance given an {@link Instance} and {@link PathContext}.
     * @param instance the current instance
     * @param context the current PathContext
     * @return a new NodeTranslator instance to use for navigation
     */
    @NotNull NodeTranslator makeTranslator(@NotNull Instance instance, @NotNull PathContext context);

    /**
     * Creates a {@link Navigator} instance given a {@link PathContext}.
     * @param context the current PathContext
     * @param agent the agent to make a navigator for
     * @return a new Navigator instance to use for navigation
     */
    @NotNull Navigator makeNavigator(@NotNull PathContext context, @NotNull Agent agent);
}
