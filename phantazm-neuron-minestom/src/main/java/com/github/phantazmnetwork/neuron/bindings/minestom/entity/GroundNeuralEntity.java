package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.api.vector.VecUtils;
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
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
                500, result -> {
            if(!result.isSuccessful()) {
                //for failed results, use steeper linear delay scaling based on the pessimistic assumption the target
                //will stay inaccessible
                return result.exploredCount() << 1;
            }
            else {
                //for successful results, use much shallower linear delay scaling
                return result.exploredCount() >> 2;
            }
        });
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
    public boolean canPathfind() {
        return super.canPathfind() && isOnGround();
    }

    @Override
    public void setTarget(@Nullable Entity entity) {
        Navigator navigator = getNavigator();
        if(entity == null) {
            navigator.setDestination(null);
        }
        else {
            navigator.setDestination(() -> {
                if(entity.isOnGround()) {
                    return VecUtils.toBlockInt(entity.getPosition());
                }

                PhysicsResult result = CollisionUtils.handlePhysics(entity, new Vec(0, -16, 0));
                if(result.isOnGround()) {
                    return VecUtils.toBlockInt(result.newPosition());
                }

                return VecUtils.toBlockInt(entity.getPosition());
            });
        }
    }
}
