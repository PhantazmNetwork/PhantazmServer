package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.commons.collection.map.HashSpatialMap;
import com.github.phantazmnetwork.commons.collection.map.SpatialMap;
import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.commons.vector.Vec3I;
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
    private final TreeSet<Node> openSet;
    private final SpatialMap<Node> graph;

    private State state;
    private Node current;
    private PathResult result;

    public AStarPathOperation(@NotNull PathContext context) {
        this.context = Objects.requireNonNull(context, "context");

        this.agent = context.getAgent();
        this.openSet = new TreeSet<>();
        this.graph = new HashSpatialMap<>();

        this.state = State.IN_PROGRESS;

        Agent agent = context.getAgent();
        openSet.add(new Node(agent.getX(), agent.getY(), agent.getZ(), 0, agent.getHeuristicCalculator()
                .compute(agent, context.getDestination()), null));
    }

    private void complete(boolean success) {
        state = success ? State.SUCCEEDED : State.FAILED;
        openSet.clear();
        graph.clear();

        Node prev = null;
        for(Node node : this.current) {
            node.setParent(prev);
            prev = node;
        }

        this.current = prev;
        result = new Result();
    }

    @Override
    public void step() {
        if(state != State.IN_PROGRESS) {
            throw new IllegalStateException("Operation has already finished");
        }

        if(!openSet.isEmpty()) {
            current = openSet.pollFirst();

            //noinspection ConstantConditions
            if(agent.reachedDestination(current, context.getDestination())) {
                complete(true);
                return;
            }

            for(Vec3I walkVector : context.getAgent().getWalker().walkVectors(current.getX(), current.getY(),
                    current.getZ())) {
                int x = current.getX() + walkVector.getX();
                int y = current.getY() + walkVector.getY();
                int z = current.getZ() + walkVector.getZ();

                Node neighbor = graph.get(x, y, z);
                if(neighbor == null) {
                    neighbor = new Node(x, y, z, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, current);
                    graph.put(x, y, z, neighbor);
                }

                float g = current.getG() + current.distanceSquared(neighbor);
                if(g < neighbor.getG()) {
                    openSet.remove(neighbor);
                    neighbor.setParent(current);
                    neighbor.setG(g);
                    neighbor.setH(agent.getHeuristicCalculator().compute(neighbor, context.getDestination()));
                    openSet.add(neighbor);
                }
            }
        }
        else {
            complete(false);
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
