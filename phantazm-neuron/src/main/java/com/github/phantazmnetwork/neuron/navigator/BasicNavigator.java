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

public class BasicNavigator implements Navigator {
    private static final float NODE_REACHED_TOLERANCE = 0.25F;

    private final PathEngine pathEngine;
    private final Agent agent;
    private final long slowTravelRecalculateInterval;
    private final long pathFailurePenalty;
    private final long minRetargetInterval;

    private Supplier<Vec3I> destinationSupplier;
    private Future<PathResult> currentOperation;
    private Node currentNode;
    private Vec3I currentDestination;
    private boolean pathSuccessful = true;

    private long lastNodeStart;
    private long lastPathfind;
    private long lastRetarget;

    public BasicNavigator(@NotNull PathEngine pathEngine, @NotNull Agent agent, long slowTravelRecalculateInterval,
                          long pathFailurePenalty, long minRetargetInterval) {
        this.pathEngine = Objects.requireNonNull(pathEngine, "pathEngine");
        this.agent = Objects.requireNonNull(agent, "agent");
        this.slowTravelRecalculateInterval = slowTravelRecalculateInterval;
        this.pathFailurePenalty = pathFailurePenalty;
        this.minRetargetInterval = minRetargetInterval;
    }

    @Override
    public void tick(long time) {
        if(destinationSupplier != null) {
            Vec3I destination = destinationSupplier.get();

            //new destination
            if(!destination.equals(currentDestination)) {
                //setting currentNode to null will trigger a pathfind, make sure we can't do this too fast
                if(currentNode == null || time - lastRetarget > minRetargetInterval) {
                    cancelOperation();

                    currentDestination = destination;
                    currentNode = null;
                    pathSuccessful = true;
                    lastRetarget = time;
                }
            }

            if(currentNode == null) {
                if(currentOperation == null) {
                    if(!pathSuccessful && time - lastPathfind < pathFailurePenalty) {
                        return;
                    }

                    currentOperation = pathEngine.pathfind(agent, destination);
                }

                if(currentOperation.isDone()) {
                    try {
                        PathResult result = currentOperation.get();
                        currentNode = result.getPath();
                        pathSuccessful = result.isSuccessful();
                        lastNodeStart = time;
                        lastPathfind = time;
                    } catch (InterruptedException | ExecutionException ignored) {}

                    currentOperation = null;
                }
            }
            else if(time - lastNodeStart > slowTravelRecalculateInterval) {
                currentNode = null;
            }
            else {
                Controller controller = agent.getController();
                Vec3I nodePosition = currentNode.getPosition();

                controller.moveTo(nodePosition);

                if(Vec3D.squaredDistance(controller.getX(), controller.getY(), controller.getZ(), nodePosition.getX()
                        + 0.5, nodePosition.getY(), nodePosition.getZ() + 0.5) < NODE_REACHED_TOLERANCE) {
                    lastNodeStart = time;
                    currentNode = currentNode.getParent();
                }
            }
        }
    }

    @Override
    public void setDestination(@Nullable Supplier<Vec3I> destinationSupplier) {
        this.destinationSupplier = destinationSupplier;
        if(destinationSupplier == null) {
            cancelOperation();
            currentNode = null;
            currentDestination = null;
            pathSuccessful = true;
            lastNodeStart = 0;
            lastRetarget = 0;
            lastPathfind = 0;
        }
    }

    private void cancelOperation() {
        if(currentOperation != null) {
            currentOperation.cancel(true);
            currentOperation = null;
        }
    }
}
