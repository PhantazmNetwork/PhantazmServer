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
import java.util.function.ToLongFunction;

/**
 * Standard {@link TrackingNavigator} implementation for ground-based movement.
 */
public class GroundNavigator extends TrackingNavigator {
    private static final double NODE_REACHED_DISTANCE = 0.25;

    private final long immobileThreshold;
    private final long missingStartDelay;
    private final ToLongFunction<? super PathResult> explorationDelayFunction;

    private Supplier<Vec3I> destinationSupplier;
    private Vec3I currentDestination;
    private Future<PathResult> currentOperation;

    private Node current;
    private Node target;
    private boolean hasPath;

    private long recalculationDelay;
    private long lastPathfind;
    private long lastMoved;

    private double lastX;
    private double lastY;
    private double lastZ;

    /**
     * Creates a new GroundNavigator for gravity-bound movement.
     * @param tracker the {@link NavigationTracker} used to record any events that occur during navigation
     * @param pathEngine the {@link PathEngine} used to calculate paths
     * @param agent the agent responsible for navigation
     * @param immobileThreshold the time beyond which a non-moving agent will be considered "stuck" and the path will be
     *                          recalculated
     * @param missingStartDelay the time to wait before path recalculation if no starting position was found
     * @param explorationDelayFunction a function used to compute the recalculation delay after a path has been found,
     *                                 which is the time that the navigator will wait before recalculating if the
     *                                 destination changes
     */
    public GroundNavigator(@NotNull NavigationTracker tracker, @NotNull PathEngine pathEngine, @NotNull Agent agent,
                           long immobileThreshold, long missingStartDelay,
                           @NotNull ToLongFunction<? super PathResult> explorationDelayFunction) {
        super(tracker, pathEngine, agent);
        this.immobileThreshold = immobileThreshold;
        this.missingStartDelay = missingStartDelay;
        this.explorationDelayFunction = Objects.requireNonNull(explorationDelayFunction,
                "explorationDelayFunction");
    }

    @Override
    public void tick(long time) {
        if(destinationSupplier != null) {
            Vec3I newDestination = destinationSupplier.get();

            boolean destinationChange = false;
            if(!newDestination.equals(currentDestination)) {
                destinationChange = true;
                this.currentDestination = newDestination;
            }

            //if we don't already have a target, we must find a path in order to call continueAlongPath
            if(target == null) {
                //try pathfinding to the current destination
                if(!tryPathfind(time) || !tryInitResult()) {
                    //we aren't allowed to pathfind right now due to cooldown or an error condition
                    return;
                }
            }
            else if(destinationChange) {
                //we already have a path, but our destination changed, so start calculating a new one (but keep moving)
                if(tryPathfind(time)) {
                    tryInitResult();
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

        recalculationDelay = explorationDelayFunction.applyAsLong(result);
        hasPath = true;

        navigationTracker.onPathfindComplete(this, result);
        return true;
    }

    private boolean tryInitResult() {
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
            if(time - lastPathfind < recalculationDelay || !agent.canPathfind()) {
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
            double currentX = controller.getX();
            double currentY = controller.getY();
            double currentZ = controller.getZ();

            if(!controller.hasControl()) {
                if(!Vec3D.equals(currentX, currentY, currentZ, lastX, lastY, lastZ)) {
                    lastMoved = time;
                }
                else if(time - lastMoved > immobileThreshold) {
                    //if we don't have any movement, stop moving along this path
                    return true;
                }
            }

            controller.advance(current, target);

            lastX = currentX;
            lastY = currentY;
            lastZ = currentZ;
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
        lastX = 0;
        lastY = 0;
        lastZ = 0;
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
    public boolean hasDestination() {
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