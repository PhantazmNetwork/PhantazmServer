package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.api.VecUtils;
import com.github.phantazmnetwork.neuron.bindings.minestom.ContextProvider;
import com.github.phantazmnetwork.neuron.bindings.minestom.DebugNavigationTracker;
import com.github.phantazmnetwork.neuron.engine.PathContext;
import com.github.phantazmnetwork.neuron.navigator.Controller;
import com.github.phantazmnetwork.neuron.navigator.GroundNavigator;
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
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.UUID;

/**
 * A {@link NeuralEntity} implementation with ground-based movement (gravitation).
 */
public class GroundNeuralEntity extends NeuralEntity {
    private static final Random ENTITY_RANDOM = new Random();

    public GroundNeuralEntity(@NotNull GroundMinestomDescriptor entityType, @NotNull UUID uuid,
                              @NotNull ContextProvider contextProvider) {
        super(entityType, uuid, contextProvider);
    }

    @Override
    protected @NotNull Navigator makeNavigator(@NotNull PathContext context) {
        return new GroundNavigator(NavigationTracker.NULL, context.getEngine(), this, 2000,
                500, result -> {
            //randomize navigation delay to make horde navigation look smoother
            double delayMultiplier = ENTITY_RANDOM.nextDouble(0.5D, 1.5D);
            if(!result.isSuccessful()) {
                //for failed results, use steeper linear delay scaling based on the pessimistic assumption the target
                //will stay inaccessible
                return (long) ((result.exploredCount() * 2L) * delayMultiplier);
            }

            //for successful results, use much shallower linear delay scaling
            return (long)((result.exploredCount() / 2L) * delayMultiplier);
        });
    }

    @Override
    protected @NotNull NodeTranslator makeTranslator(@NotNull Instance instance, @NotNull PathContext context) {
        return new GroundTranslator(context.getCollider(), (GroundMinestomDescriptor) getDescriptor());
    }

    @Override
    protected @NotNull Controller makeController() {
        return new GroundController(this, ((GroundMinestomDescriptor)getDescriptor()).getStepHeight());
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
                PhysicsResult result = CollisionUtils.handlePhysics(entity, new Vec(0, -16, 0));
                if(result.isOnGround()) {
                    return VecUtils.toBlockInt(result.collidedBlockY().add(0, result.blockTypeY().registry()
                            .collisionShape().relativeEnd().y(), 0));
                }

                return VecUtils.toBlockInt(entity.getPosition());
            });
        }
    }
}
