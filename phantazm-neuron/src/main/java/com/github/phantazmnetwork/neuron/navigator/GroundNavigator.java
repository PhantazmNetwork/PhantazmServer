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

    private Node current;
    private Node target;

    public GroundNavigator(@NotNull PathEngine pathEngine, @NotNull Agent agent) {
        this.pathEngine = Objects.requireNonNull(pathEngine, "pathEngine");
        this.agent = Objects.requireNonNull(agent, "agent");
    }

    @Override
    public void tick(long time) {
        if(destinationSupplier != null) {
            Controller controller = agent.getController();

            if(target == null) {
                if(currentOperation == null) {
                    currentOperation = pathEngine.pathfind(agent, destinationSupplier.get());
                }

                if(currentOperation.isDone()) {
                    PathResult result = null;
                    try {
                        result = currentOperation.get();
                        currentOperation = null;
                    } catch (InterruptedException | ExecutionException ignored) {}

                    if(result != null) {
                        target = computeActualTarget(controller, result.getStart());
                        current = computeActualStart(controller, target);

                        //single node path
                        if(target == null) {
                            target = current;
                        }
                    }
                    else {
                        //path was cancelled or an exception was thrown
                        return;
                    }
                }
            }

            if(target != null) {
                continueAlongPath();
            }
        }
    }

    private void continueAlongPath() {
        Controller controller = agent.getController();
        if(withinDistance(controller, target)) {
            current = target;
            target = current.getParent();
        }

        if(target != null) {
            controller.advance(current, target);
        }
        else {
            current = null;
        }
    }

    private static Node computeActualTarget(Controller controller, Node result) {
        for(Node node : result) {
            if(withinDistance(controller, node)) {
                return node.getParent();
            }
        }

        return null;
    }

    private static Node computeActualStart(Controller controller, Node next) {
        double controllerY = controller.getY();
        Node node = new Node(Vec3I.floored(controller.getX(), controllerY, controller.getZ()), 0, 0, next);
        node.setHeightOffset((float) (controllerY - Math.floor(controllerY)));
        return node;
    }

    private static boolean withinDistance(Controller controller, Node node) {
        Vec3I nodePosition = node.getPosition();
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