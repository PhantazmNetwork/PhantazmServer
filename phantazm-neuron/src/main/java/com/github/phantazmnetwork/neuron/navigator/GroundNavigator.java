package com.github.phantazmnetwork.neuron.navigator;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.engine.PathEngine;
import com.github.phantazmnetwork.neuron.node.Node;
import com.github.phantazmnetwork.neuron.operation.PathResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public class GroundNavigator extends TrackingNavigator {
    private static final double MOVEMENT_EPSILON = 1E-6;
    private static final double NODE_REACHED_DISTANCE = 0.25;

    private final long immobileThreshold;
    private final long missingStartDelay;
    private final double exploredDelayMultiplier;

    private Supplier<Vec3I> destinationSupplier;
    private Vec3I currentDestination;
    private Future<PathResult> currentOperation;

    private Node current;
    private Node target;
    private boolean hasPath;

    private long recalculationDelay;
    private long lastPathfind;
    private long lastMoved;

    public GroundNavigator(@NotNull NavigationTracker tracker, @NotNull PathEngine pathEngine, @NotNull Agent agent,
                           long immobileThreshold, long missingStartDelay, double exploredDelayMultiplier) {
        super(tracker, pathEngine, agent);
        this.immobileThreshold = immobileThreshold;
        this.missingStartDelay = missingStartDelay;
        this.exploredDelayMultiplier = exploredDelayMultiplier;
    }

    @Override
    public void tick(long time) {
        if(destinationSupplier != null) {
            Vec3I newDestination = destinationSupplier.get();

            boolean destinationChange = false;
            if(!newDestination.equals(currentDestination)) {
                //destination change
                destinationChange = true;
                this.currentDestination = newDestination;
            }

            //if we don't already have a target, we must find a path in order to call continueAlongPath
            if(target == null) {
                //try pathfinding to the current destination
                if(!tryPathfind(time)) {
                    //we aren't allowed to pathfind right now due to cooldown
                    return;
                }

                //if we fail to properly initialize the result, return (can happen if we're still calculating a path)
                if(!tryGetResult()) {
                    return;
                }
            }
            else if(destinationChange && !agent.getController().isJumping()) {
                //we already have a path, but our destination changed, so start calculating a new one (but keep moving)
                if(tryPathfind(time)) {
                    tryGetResult();
                }
            }

            if(current == target || continueAlongPath(time)) {
                //path has ended
                navigationTracker.onDestinationReached(this);
                reset(time);
            }
        }
    }

    private boolean initializePath(PathResult result) {
        Controller controller = agent.getController();
        Node pathStart = result.getStart();

        //search for an appropriate starting node
        Node start = null;
        for(Node node : pathStart) {
            if(withinDistance(controller, node)) {
                start = node;
                break;
            }
        }

        if(start == null) {
            //failed to find starting node
            navigationTracker.onNavigationError(this, NavigationTracker.ErrorType.NO_START);
            recalculationDelay = missingStartDelay;
            return false;
        }

        current = start;

        //account for single-node path
        Node currentParent = start.getParent();
        target = currentParent == null ? start : currentParent;

        recalculationDelay = Math.round(result.exploredCount() * exploredDelayMultiplier);
        hasPath = true;

        navigationTracker.onPathfindComplete(this, result);
        return true;
    }

    private boolean tryGetResult() {
        if(currentOperation.isDone()) {
            PathResult result = null;
            try {
                result = currentOperation.get();
                currentOperation = null;
            } catch (ExecutionException | InterruptedException ignored) {}

            return result != null && initializePath(result);
        }

        return false;
    }

    private boolean tryPathfind(long time) {
        if(currentOperation == null) {
            if(time - lastPathfind < recalculationDelay) {
                return false;
            }

            lastPathfind = time;
            currentOperation = pathEngine.pathfind(agent, currentDestination);
            navigationTracker.onPathfind(this);
        }

        return true;
    }

    private boolean continueAlongPath(long time) {
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

            //if we're jumping, we'll have motion from physics engine, not the controller advancing, so only check when
            //we're on the ground
            if(!controller.isJumping()) {
                //detect agents that are not moving
                if(Vec3D.fuzzyEquals(oX, oY, oZ, controller.getX(), controller.getY(), controller.getZ(),
                        MOVEMENT_EPSILON)) {
                    //if we aren't moving for too long, consider the path complete (so we pathfind again)
                    if(time - lastMoved > immobileThreshold) {
                        navigationTracker.onNavigationError(this, NavigationTracker.ErrorType.STUCK);
                        return true;
                    }
                }
                else {
                    lastMoved = time;
                }
            }

            return false;
        }

        return true;
    }

    private void reset(long time) {
        currentDestination = null;
        currentOperation = null;
        current = null;
        target = null;
        hasPath = false;
        lastPathfind = time;
        lastMoved = time;
    }

    private static boolean withinDistance(Controller controller, Node node) {
        Vec3I nodePosition = node.getPosition();
        return Vec3D.squaredDistance(controller.getX(), controller.getY(), controller.getZ(), nodePosition.getX() +
                node.getXOffset(), nodePosition.getY() + node.getYOffset(), nodePosition.getZ() + node
                .getZOffset()) < NODE_REACHED_DISTANCE;
    }

    @Override
    public void setDestination(@Nullable Supplier<Vec3I> destinationSupplier) {
        this.destinationSupplier = destinationSupplier;
        if(destinationSupplier == null) {
            reset(0);
        }
    }

    @Override
    public @NotNull Agent getAgent() {
        return agent;
    }

    @Override
    public boolean hasPath() {
        return hasPath;
    }

    @Override
    public @NotNull Vec3I getDestination() {
        if(currentDestination == null) {
            throw new IllegalStateException("Cannot get the destination for a Navigator that has no path");
        }

        return currentDestination;
    }
}