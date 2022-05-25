package com.github.phantazmnetwork.mob.descriptor;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.GroundMinestomDescriptor;
import com.github.phantazmnetwork.neuron.engine.PathContext;
import com.github.phantazmnetwork.neuron.navigator.GroundNavigator;
import com.github.phantazmnetwork.neuron.navigator.NavigationTracker;
import com.github.phantazmnetwork.neuron.navigator.Navigator;
import com.github.phantazmnetwork.neuron.node.Node;
import com.github.phantazmnetwork.neuron.operation.PathResult;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public abstract class GroundPhantazmMobDescriptor implements GroundMinestomDescriptor {

    private final EntityType entityType;

    private final String id;

    private final NavigationTracker navigationTracker;

    public GroundPhantazmMobDescriptor(@NotNull EntityType entityType, @NotNull String id) {
        this.entityType = Objects.requireNonNull(entityType, "entityType");
        this.id = Objects.requireNonNull(id, "id");
        this.navigationTracker = new NavigationTracker() {
            @Override
            public void onPathfind(@NotNull Navigator navigator) {

            }

            @Override
            public void onPathfindComplete(@NotNull Navigator navigator, @NotNull Node pathStart, @Nullable PathResult result) {

            }

            @Override
            public void onDestinationReached(@NotNull Navigator navigator) {
                navigator.setDestination(() -> chooseDestination(navigator.getAgent()));
            }

            @Override
            public void onNavigationError(@NotNull Navigator navigator, @Nullable Node pathStart, @NotNull ErrorType errorType) {

            }
        };
    }

    @Override
    public @NotNull EntityType getEntityType() {
        return entityType;
    }

    @Override
    public @NotNull String getID() {
        return id;
    }

    @Override
    public @NotNull Navigator makeNavigator(@NotNull PathContext context, @NotNull Agent agent) {
        return new GroundNavigator(navigationTracker, context.getEngine(), agent, 2000,
                500, result -> {
            //randomize navigation delay to make horde navigation look smoother
            double delayMultiplier = ThreadLocalRandom.current().nextDouble(0.5D, 1.5D);
            if(!result.isSuccessful()) {
                //for failed results, use steeper linear delay scaling based on the pessimistic assumption the target
                //will stay inaccessible
                return (long) ((result.exploredCount() * 2L) * delayMultiplier);
            }

            //for successful results, use much shallower linear delay scaling
            return (long)((result.exploredCount() / 2L) * delayMultiplier);
        });
    }

    protected abstract @Nullable Vec3I chooseDestination(@NotNull Agent agent);

}
