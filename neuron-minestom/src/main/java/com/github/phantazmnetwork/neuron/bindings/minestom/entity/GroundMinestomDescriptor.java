package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.api.VecUtils;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.agent.GroundDescriptor;
import com.github.phantazmnetwork.neuron.engine.PathContext;
import com.github.phantazmnetwork.neuron.navigator.Controller;
import com.github.phantazmnetwork.neuron.navigator.GroundNavigator;
import com.github.phantazmnetwork.neuron.navigator.NavigationTracker;
import com.github.phantazmnetwork.neuron.navigator.Navigator;
import com.github.phantazmnetwork.neuron.node.GroundTranslator;
import com.github.phantazmnetwork.neuron.node.NodeTranslator;
import net.kyori.adventure.key.Key;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A {@link MinestomDescriptor} for gravity-bound entities.
 *
 * @see GroundDescriptor
 * @see MinestomDescriptor
 */
public interface GroundMinestomDescriptor extends MinestomDescriptor, GroundDescriptor {

    /**
     * The unique serial key for {@link GroundMinestomDescriptor}s.
     */
    Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "descriptor.ground");

    /**
     * Creates a new GroundMinestomDescriptor using default values, the given {@link EntityType}, and the given id.
     *
     * @param type the EntityType to use
     * @param id   the id to use
     * @return a new GroundMinestomDescriptor implementation
     */
    static @NotNull GroundMinestomDescriptor of(@NotNull EntityType type, @NotNull String id) {
        return new GroundMinestomDescriptor() {
            @Override
            public @NotNull EntityType getEntityType() {
                return type;
            }

            @Override
            public @NotNull String getID() {
                return id;
            }
        };
    }

    @Override
    default float getJumpHeight() {
        return 1F;
    }

    @Override
    default float getFallTolerance() {
        return 3F;
    }

    @Override
    default @NotNull Key key() {
        return SERIAL_KEY;
    }

    /**
     * Returns the maximum "step height" for this agent. For vertical distances greater less than the step height,
     * the agent should teleport up instantaneously instead of jumping.
     *
     * @return the step height for this agent
     */
    default float getStepHeight() {
        return 0.5F;
    }

    @Override
    default @NotNull Vec3I computeTargetPosition(@NotNull Entity targetEntity) {
        PhysicsResult result =
                CollisionUtils.handlePhysics(targetEntity, new Vec(0, -computeDownwardsCheckDistance(targetEntity), 0));
        if (result.isOnGround()) {
            return VecUtils.toBlockInt(result.collidedBlockY()
                                             .add(0, result.blockTypeY().registry().collisionShape().relativeEnd().y(),
                                                  0
                                             ));
        }

        return VecUtils.toBlockInt(targetEntity.getPosition());
    }

    /**
     * Computes the distance that will be searched below an entity in order to determine the block it is above. This is
     * the point to which gravity-bound entities will navigate.
     *
     * @param target the target entity
     * @return the downwards check distance
     */
    default double computeDownwardsCheckDistance(@NotNull Entity target) {
        if (target.isOnGround()) {
            return 1;
        }

        return 16;
    }

    @Override
    default boolean canPathfind(@NotNull NeuralEntity entity) {
        return entity.isOnGround();
    }

    default @NotNull Controller makeController(@NotNull NeuralEntity entity) {
        return new GroundController(entity, getStepHeight());
    }

    @Override
    default @NotNull NodeTranslator makeTranslator(@NotNull Instance instance, @NotNull PathContext context) {
        return new GroundTranslator(context.getCollider(), this);
    }

    @Override
    default @NotNull Navigator makeNavigator(@NotNull PathContext context, @NotNull Agent agent) {
        return new GroundNavigator(NavigationTracker.NULL, context.getEngine(), agent, 2000, 500, result -> {
            //randomize navigation delay to make horde navigation look smoother
            double delayMultiplier = ThreadLocalRandom.current().nextDouble(0.5D, 1.5D);
            if (!result.isSuccessful()) {
                //for failed results, use steeper linear delay scaling based on the pessimistic assumption the target
                //will stay inaccessible
                return (long)((result.exploredCount() * 2L) * delayMultiplier);
            }

            //for successful results, use much shallower linear delay scaling
            return (long)((result.exploredCount() / 2L) * delayMultiplier);
        });
    }
}
