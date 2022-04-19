package com.github.phantazmnetwork.neuron.navigator;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.engine.PathEngine;
import com.github.phantazmnetwork.neuron.node.Node;
import com.github.phantazmnetwork.neuron.operation.PathResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public class GroundNavigator implements Navigator {
    private static final double NODE_REACHED_DISTANCE = 0.25;

    private final PathEngine pathEngine;
    private final Agent agent;

    private Supplier<Vec3I> destinationSupplier;
    private Future<PathResult> currentOperation;
    private Node node;

    public GroundNavigator(@NotNull PathEngine pathEngine, @NotNull Agent agent) {
        this.pathEngine = Objects.requireNonNull(pathEngine, "pathEngine");
        this.agent = Objects.requireNonNull(agent, "agent");
    }

    @Override
    public void tick(long time) {
        if(destinationSupplier != null) {
            if(node == null) {
                if(currentOperation == null) {
                    currentOperation = pathEngine.pathfind(agent, destinationSupplier.get());
                }

                PathResult result = null;
                try {
                    result = currentOperation.get();
                    currentOperation = null;
                } catch (InterruptedException | ExecutionException ignored) {}

                if(result != null) {
                    node = result.getStart();
                }
                else {
                    //path was cancelled or an exception was thrown
                    return;
                }
            }

            Controller controller = agent.getController();
            controller.advance(node);

            if(hasReached(node)) {
                node = node.getParent();
            }
        }
    }

    private boolean hasReached(Node node) {
        Vec3I nodePosition = node.getPosition();
        Controller controller = agent.getController();
        return Vec3D.squaredDistance(controller.getX(), controller.getY(), controller.getZ(), nodePosition.getX() +
                0.5, nodePosition.getY() + node.getHeightOffset(), nodePosition.getZ() + 0.5) <
                NODE_REACHED_DISTANCE;
    }

    @Override
    public void setDestination(@Nullable Supplier<Vec3I> destinationSupplier) {
        this.destinationSupplier = destinationSupplier;
        if(destinationSupplier == null) {
            //setting to null == cancelling, so just reset everything
            cancelOperation();
            node = null;
        }
    }

    private void cancelOperation() {
        if(currentOperation != null) {
            currentOperation.cancel(true);
            try {
                currentOperation.get();
            } catch (InterruptedException | ExecutionException e) {
                //TODO: log ExecutionException here (InterruptedException should never happen though)
            }

            currentOperation = null;
        }
    }
}