package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.bindings.minestom.ContextProvider;
import com.github.phantazmnetwork.neuron.bindings.minestom.StepDirections;
import com.github.phantazmnetwork.neuron.engine.PathContext;
import com.github.phantazmnetwork.neuron.navigator.GroundNavigator;
import com.github.phantazmnetwork.neuron.navigator.Controller;
import com.github.phantazmnetwork.neuron.navigator.NavigationTracker;
import com.github.phantazmnetwork.neuron.navigator.Navigator;
import com.github.phantazmnetwork.neuron.node.GroundTranslator;
import com.github.phantazmnetwork.neuron.node.NodeTranslator;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A {@link NeuralEntity} implementation with ground-based movement (gravitation).
 */
public class GroundNeuralEntity extends NeuralEntity {
    public GroundNeuralEntity(@NotNull GroundMinestomDescriptor entityType, @NotNull UUID uuid,
                              @NotNull ContextProvider contextProvider) {
        super(entityType, uuid, contextProvider);
    }

    @Override
    protected @NotNull Navigator makeNavigator(@NotNull PathContext context) {
        return new GroundNavigator(NavigationTracker.NULL, context.getEngine(), this, 500,
                500, 1.5);
    }

    @Override
    protected @NotNull NodeTranslator makeTranslator(@NotNull Instance instance, @NotNull PathContext context) {
        return new GroundTranslator(context.getCollider(), (GroundMinestomDescriptor) getDescriptor());
    }

    @Override
    protected @NotNull Controller makeController() {
        return new GroundController(this, 0.5F);
    }

    @Override
    protected @NotNull Iterable<Vec3I> getStepDirections() {
        return StepDirections.WALK;
    }

    @Override
    protected boolean canPathfind() {
        return super.canPathfind() && isOnGround();
    }
}
