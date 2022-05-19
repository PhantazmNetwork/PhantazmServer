package com.github.phantazmnetwork.neuron.bindings.minestom;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.navigator.Controller;
import com.github.phantazmnetwork.neuron.navigator.NavigationTracker;
import com.github.phantazmnetwork.neuron.navigator.Navigator;
import com.github.phantazmnetwork.neuron.node.Node;
import com.github.phantazmnetwork.neuron.operation.PathResult;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Objects;

public class DebugNavigationTracker implements NavigationTracker {
    private final Logger logger;
    private final Instance instance;

    public DebugNavigationTracker(@NotNull Logger logger, @NotNull Instance instance) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.instance = Objects.requireNonNull(instance, "instance");
    }

    @Override
    public void onPathfind(@NotNull Navigator navigator) {

    }

    @Override
    public void onPathfindComplete(@NotNull Navigator navigator, @NotNull Node pathStart, @Nullable PathResult result) {
        if(result != null && !result.isSuccessful()) {
            logger.error("Failed path: ");

            for(Node node : pathStart) {
                logger.error("\t" + node);
                Vec3I nodePos = node.getPosition();
                instance.setBlock(nodePos.getX(), nodePos.getY(), nodePos.getZ(), Block.BIRCH_SAPLING);
            }
        }
    }

    @Override
    public void onDestinationReached(@NotNull Navigator navigator) {

    }

    @Override
    public void onNavigationError(@NotNull Navigator navigator, @Nullable Node pathStart, @NotNull ErrorType errorType) {
        Controller controller = navigator.getAgent().getController();
        logger.error("Navigation error for navigator at position " + Vec3D.of(controller.getX(), controller.getY(),
                controller.getZ()) + ", navigating to " + navigator.getDestination() + ": ERROR_TYPE " + errorType +
                ", starting node " + pathStart);

        if (pathStart != null) {
            double nearest = Double.POSITIVE_INFINITY;
            Node nearestNode = null;
            for (Node node : pathStart) {
                Vec3I nodePos = node.getPosition();
                double distance = Vec3D.squaredDistance(nodePos.getX() + 0.5, nodePos.getY() + node.getYOffset(),
                        nodePos.getZ() + 0.5, controller.getX(), controller.getY(), controller.getZ());
                if (distance < nearest) {
                    nearest = distance;
                    nearestNode = node;
                }
            }

            logger.error("Nearest node: " + nearestNode + ", was " + Math.sqrt(nearest) + " blocks away");
        }
    }
}
