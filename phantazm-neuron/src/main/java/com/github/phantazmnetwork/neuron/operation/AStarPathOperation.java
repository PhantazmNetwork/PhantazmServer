package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.HeuristicCalculator;
import com.github.phantazmnetwork.neuron.collection.NodeQueue;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AStarPathOperation implements PathOperation {
    private class Result implements PathResult {
        @Override
        public @NotNull PathOperation getOperation() {
            return AStarPathOperation.this;
        }

        @Override
        public @NotNull Iterable<Node> getPath() {
            return AStarPathOperation.this.current;
        }
    }

    private final PathContext context;
    private final Agent agent;
    private final NodeQueue openSet;
    private final Map<Node, Node> graph;

    private State state;
    private Node current;
    private PathResult result;

    public AStarPathOperation(@NotNull PathContext context) {
        this.context = Objects.requireNonNull(context, "context");

        this.agent = context.getAgent();
        this.openSet = new NodeQueue();
        this.graph = new Object2ObjectRBTreeMap<>();

        this.state = State.IN_PROGRESS;

        HeuristicCalculator heuristicCalculator = context.getAgent().getHeuristicCalculator();
        Vec3I start = context.getStartPosition();
        Vec3I destination = context.getDestination();

        openSet.enqueue(new Node(start.getX(), start.getY(), start.getZ(), 0, heuristicCalculator.calculate(start
                        .getX(), start.getY(), start.getZ(), destination.getX(), destination.getY(),
                destination.getZ()), null));
    }

    private void complete(State state) {
        this.state = state;
        openSet.clear();
        graph.clear();
        current = current.invert();
        result = new Result();
    }

    @Override
    public void step() {
        if(state != State.IN_PROGRESS) {
            throw new IllegalStateException("Operation has already finished");
        }

        if(!openSet.isEmpty()) {
            current = openSet.dequeue();

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

                //node objects only take into account position for hashCode/equals, so use a temporary node as a key
                Node neighbor = graph.get(new Node(x, y, z));
                if(neighbor == null) {
                    neighbor = new Node(x, y, z, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, current);

                    /*
                    this may seem strange, but it allows us to avoid needing to create and store persistent nodes whose
                    only purpose is to act as a key. done like this, the same object is used as both key and value,
                    which halves the memory footprint of the graph
                     */
                    graph.put(neighbor, neighbor);
                }

                float g = current.getG() + current.distanceSquared(neighbor);
                if(g < neighbor.getG()) {
                    neighbor.setParent(current);
                    neighbor.setG(g);
                    neighbor.setH(agent.getHeuristicCalculator().calculate(neighbor.getX(), neighbor.getY(),
                            neighbor.getZ(), destination.getX(), destination.getY(), destination.getZ()));
                    if(neighbor.isOnHeap()) {
                        openSet.changed(neighbor);
                    }
                }
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
