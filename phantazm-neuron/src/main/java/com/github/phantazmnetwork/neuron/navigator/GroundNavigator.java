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

    private final long immobileThreshold;
    private final long missingStartDelay;

    private Supplier<Vec3I> destinationSupplier;
    private Future<PathResult> currentOperation;

    private Node current;
    private Node target;

    private double lastPathfind;
    private double recalculationDelay;

    private long notMovingStart;

    public GroundNavigator(@NotNull PathEngine pathEngine, @NotNull Agent agent, long immobileThreshold,
                           long missingStartDelay) {
        this.pathEngine = Objects.requireNonNull(pathEngine, "pathEngine");
        this.agent = Objects.requireNonNull(agent, "agent");
        this.notMovingStart = -1;

        this.immobileThreshold = immobileThreshold;
        this.missingStartDelay = missingStartDelay;
    }

    @Override
    public void tick(long time) {
        if(destinationSupplier != null) {
            if(target == null) {
                if(currentOperation == null) {
                    if(time - lastPathfind < recalculationDelay) {
                        //don't recalculate the path again too soon
                        return;
                    }

                    currentOperation = pathEngine.pathfind(agent, destinationSupplier.get());
                    lastPathfind = time;
                    recalculationDelay = 0;
                }

                if(currentOperation.isDone()) {
                    PathResult result = null;
                    try {
                        result = currentOperation.get();
                        currentOperation = null;
                    } catch (InterruptedException | ExecutionException ignored) {}

                    if(result != null) {
                        Controller controller = agent.getController();
                        Node start = result.getStart();

                        //try to find a node that we're on top of in case of de-sync
                        Node nearest = null;
                        for(Node node : start) {
                            if(withinDistance(controller, node)) {
                                nearest = node;
                                break;
                            }
                        }

                        if(nearest != null) {
                            //we found a starting node
                            current = nearest;
                            Node parent = nearest.getParent();

                            //in case of single-node path, current == target
                            target = parent == null ? nearest : target;
                            recalculationDelay = result.exploredCount();
                        }
                        else {
                            //failed to find starting node — delay path recalculation
                            recalculationDelay = missingStartDelay;
                            return;
                        }
                    }
                    else {
                        //path was cancelled or an exception was thrown
                        return;
                    }
                }
                else {
                    //path operation is still running (can take multiple ticks)
                    return;
                }
            }

            continueAlongPath(time);
        }
    }

    private void continueAlongPath(long time) {
        Controller controller = agent.getController();
        if(withinDistance(controller, target)) {
            current = target;
            target = current.getParent();
        }

        if(target != null) {
            double oX = controller.getX();
            double oY = controller.getY();
            double oZ = controller.getZ();
            controller.advance(current, target);

            //if we're jumping, we'll have motion so only check when we're on the ground
            if(!controller.isJumping()) {
                //detect agents that are not moving
                if(Vec3D.fuzzyEquals(oX, oY, oZ, controller.getX(), controller.getY(), controller.getZ(),
                        1E-6)) {
                    if(notMovingStart == -1) {
                        notMovingStart = time;
                    }
                    else if(time - notMovingStart > immobileThreshold) {
                        //if we aren't moving for too long, recalculate eventually, subject to any recalculation delay
                        target = null;
                        current = null;
                        notMovingStart = -1;
                    }
                }
                else {
                    notMovingStart = -1;
                }
            }
        }
        else {
            current = null;
        }
    }

    private static boolean withinDistance(Controller controller, Node node) {
        Vec3I nodePosition = node.getPosition();
        return Vec3D.squaredDistance(controller.getX(), controller.getY(), controller.getZ(), nodePosition.getX() +
                0.5, nodePosition.getY() + node.getYOffset(), nodePosition.getZ() + 0.5) <
                NODE_REACHED_DISTANCE;
    }

    @Override
    public void setDestination(@Nullable Supplier<Vec3I> destinationSupplier) {
        this.destinationSupplier = destinationSupplier;
        if(destinationSupplier == null) {
            //setting to null == cancelling, so just reset everything
            cancelOperation();
            target = null;
        }
    }

    private void cancelOperation() {
        if(currentOperation != null) {
            currentOperation.cancel(true);
            try {
                currentOperation.get();
            } catch (InterruptedException | ExecutionException ignored) {}

            currentOperation = null;
        }
    }
}