package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.agent.PhysicalDescriptor;
import com.github.phantazmnetwork.neuron.engine.PathContext;
import com.github.phantazmnetwork.neuron.navigator.Controller;
import com.github.phantazmnetwork.neuron.navigator.Navigator;
import com.github.phantazmnetwork.neuron.node.Calculator;
import com.github.phantazmnetwork.neuron.node.NodeTranslator;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import javax.naming.ldap.Control;

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

    @NotNull Vec3I computeTargetPosition(@NotNull Entity targetEntity);

    boolean canPathfind(@NotNull NeuralEntity entity);

    /**
     * Creates a {@link Controller} suitable for making the given entity move along a path.
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
     * @return a new Navigator instance to use for navigation
     */
    @NotNull Navigator makeNavigator(@NotNull PathContext context, @NotNull Agent agent);
}
