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
            Vec3I nodePosition = node.getPosition();

            //delta == distance moved by entity this tick
            Vec3D delta = controller.advance(nodePosition);

            if(nodePosition.getY() > agent.getController().getY()) {
                System.out.println("Jumping");
                controller.setVelocity(Vec3D.of(0, 2.5f, 0));
            }

            if(hasReached(nodePosition)) {
                node = node.getParent();
            }
        }
    }

    private boolean hasReached(Vec3I nodePosition) {
        Controller controller = agent.getController();
        return Vec3D.squaredDistance(controller.getX(), 0, controller.getZ(), nodePosition.getX() + 0.5,
                0, nodePosition.getZ() + 0.5) < 0.25;
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