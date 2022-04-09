package com.github.phantazmnetwork.neuron.navigator;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.engine.PathEngine;
import com.github.phantazmnetwork.neuron.node.Node;
import com.github.phantazmnetwork.neuron.operation.PathResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public class BasicNavigator implements Navigator {
    private static final float NODE_REACHED_TOLERANCE = 0.25F;

    private final PathEngine pathEngine;
    private final Agent agent;

    private Supplier<Vec3I> destinationSupplier;
    private Future<PathResult> currentOperation;
    private Node currentNode;

    public BasicNavigator(@NotNull PathEngine pathEngine, @NotNull Agent agent, long slowTravelRecalculateInterval,
                          long pathFailurePenalty, long minRetargetInterval) {
        this.pathEngine = Objects.requireNonNull(pathEngine, "pathEngine");
        this.agent = Objects.requireNonNull(agent, "agent");
    }

    @Override
    public void tick(long time) {
        if(destinationSupplier != null) {
            Controller controller = agent.getController();
            if(currentNode == null) {
                if(currentOperation == null) {
                    System.out.println("Calculating path...");
                    currentOperation = pathEngine.pathfind(agent, destinationSupplier.get());
                }

                try {
                    PathResult result = currentOperation.get();
                    currentOperation = null;

                    if(result != null) {
                        System.out.println("Got PathResult. Time elapsed: " + (time - System.currentTimeMillis()) + "ms");

                        currentNode = result.getPath().getParent();
                        if(currentNode != null) {
                            controller.moveTo(currentNode.getPosition());
                            System.out.println(Arrays.toString(currentNode.toRandomAccessList().toArray()));
                            System.out.println();
                        }
                        else {
                            System.out.println("Current node is null");
                        }

                        return;
                    }
                } catch (InterruptedException | ExecutionException ignored) {}
            }

            if(currentNode != null) {
                Vec3I nodePosition = currentNode.getPosition();
                double distance;
                if((distance = Vec3D.squaredDistance(controller.getX(), controller.getY(), controller.getZ(), nodePosition
                        .getX() + 0.5, nodePosition.getY(), nodePosition.getZ() + 0.5)) <
                        NODE_REACHED_TOLERANCE) {
                    System.out.println("Advancing node. Distance: " + distance);
                    System.out.println();

                    currentNode = currentNode.getParent();

                    if(currentNode != null) {
                        controller.moveTo(currentNode.getPosition());
                    }
                    else {
                        currentOperation = null;
                    }
                }
            }
        }
    }

    @Override
    public void setDestination(@Nullable Supplier<Vec3I> destinationSupplier) {
        this.destinationSupplier = destinationSupplier;
        if(destinationSupplier == null) {
            System.out.println("Cancelling operation");
            //setting to null == cancelling, so just reset everything
            cancelOperation();
            currentNode = null;
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