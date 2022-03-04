package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Calculator;
import com.github.phantazmnetwork.neuron.collection.NodeQueue;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AStarPathOperation implements PathOperation {
    private class Result implements PathResult {
        private final Node node;

        private Result(Node node) {
            this.node = node;
        }

        @Override
        public @NotNull PathOperation getOperation() {
            return AStarPathOperation.this;
        }

        @Override
        public @NotNull Iterable<Node> getPath() {
            return node;
        }
    }

    private final PathContext context;
    private final NodeQueue openSet;
    private final Map<Node, Node> graph;

    private State state;
    private Node current;
    private Node best;
    private PathResult result;

    public AStarPathOperation(@NotNull PathContext context) {
        this.context = Objects.requireNonNull(context, "context");

        this.openSet = new NodeQueue();
        this.graph = new Object2ObjectRBTreeMap<>();

        this.state = State.IN_PROGRESS;

        Calculator heuristicCalculator = context.getAgent().getCalculator();
        Vec3I start = context.getStartPosition();
        Vec3I destination = context.getDestination();

        openSet.enqueue(best = new Node(start.getX(), start.getY(), start.getZ(), 0,
                heuristicCalculator.heuristic(start.getX(), start.getY(), start.getZ(), destination.getX(),
                        destination.getY(), destination.getZ()), null));
    }

    private void complete(State state) {
        this.state = state;
        openSet.clear();
        graph.clear();
        result = new Result(state == State.SUCCEEDED ? current.invert() : best.invert());
    }

    @Override
    public void step() {
        if(state != State.IN_PROGRESS) {
            throw new IllegalStateException("Operation has already finished");
        }

        if(!openSet.isEmpty()) {
            current = openSet.dequeue();

            Agent agent = context.getAgent();
            Calculator calculator = agent.getCalculator();
            Vec3I destination = context.getDestination();
            if(agent.reachedDestination(current.getX(), current.getY(), current.getZ(), destination.getX(), destination
                    .getY(), destination.getZ())) {
                complete(State.SUCCEEDED);
                return;
            }

            for(Vec3I walkVector : context.getAgent().getWalker().walkVectors(current.getX(), current.getY(),
                    current.getZ())) {
                int x = current.getX() + walkVector.getX();
                int y = current.getY() + walkVector.getY();
                int z = current.getZ() + walkVector.getZ();

                Node access = new Node(x, y, z, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, current);
                Node neighbor = graph.get(access);
                if(neighbor == null) {
                    neighbor = access;

                    /*
                    this may seem strange, but it allows us to avoid needing to create and store persistent nodes whose
                    only purpose is to act as a key. done like this, the same object is used as both key and value,
                    which halves the memory footprint of the graph
                     */
                    graph.put(neighbor, neighbor);
                }

                float g = current.getG() + calculator.distance(current.getX(), current.getY(), current.getZ(),
                        neighbor.getX(), neighbor.getY(), neighbor.getZ());
                if(g < neighbor.getG()) {
                    neighbor.setParent(current);
                    neighbor.setG(g);
                    neighbor.setH(calculator.heuristic(neighbor.getX(), neighbor.getY(), neighbor.getZ(),
                            destination.getX(), destination.getY(), destination.getZ()));
                    if(neighbor.isOnHeap()) {
                        openSet.changed(neighbor);
                    }
                }
            }

            //compare heuristic
            if(current.getF() < best.getF()) {
                best = current;
            }
        }
        else {
            complete(State.FAILED);
        }
    }

    @Override
    public @NotNull State getState() {
        return state;
    }

    @Override
    public @NotNull PathContext getContext() {
        return context;
    }

    @Override
    public @NotNull PathResult getResult() {
        if(result == null) {
            throw new IllegalStateException("Operation has not yet completed");
        }

        return result;
    }
}
