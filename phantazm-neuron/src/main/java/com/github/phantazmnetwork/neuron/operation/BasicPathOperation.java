package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.commons.collection.map.HashSpatialMap;
import com.github.phantazmnetwork.commons.collection.map.SpatialMap;
import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.agent.HeuristicCalculator;
import com.github.phantazmnetwork.neuron.engine.Pathfinder;
import com.github.phantazmnetwork.neuron.vector.Vec3I;
import com.github.phantazmnetwork.neuron.world.Space;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;

public class BasicPathOperation implements PathOperation {
    private static final int OPEN_HEAP_INITIAL_CAPACITY = 32;

    private final PathContext context;
    private final Queue<Node> openHeap;
    private final SpatialMap<Node> visited;

    private State state;
    private Node currentNode;

    public BasicPathOperation(@NotNull PathContext context) {
        this.context = Objects.requireNonNull(context, "context");
        this.openHeap = new PriorityQueue<>(OPEN_HEAP_INITIAL_CAPACITY);
        this.visited = new HashSpatialMap<>();
        this.state = State.IN_PROGRESS;

        Agent agent = context.getAgent();
        openHeap.add(new Node(agent.getX(), agent.getY(), agent.getZ(), 0, agent.getHeuristicCalculator()
                .compute(agent, context.getDestination()), null));
    }

    private void complete(boolean success) {

    }

    @Override
    public void step() {
        if(state != State.IN_PROGRESS) {
            throw new IllegalStateException("Operation has already finished");
        }

        if(!openHeap.isEmpty()) {
            currentNode = openHeap.remove();
            Node[] nodes = context.getAgent().getNodeExplorer().expandNode(currentNode);
            for(Node node : nodes) {
                if(node == null) {
                    break;
                }

                if(visited.containsKey(node.getX(), node.getY(), node.getZ())) {
                    continue;
                }

                Node existing = null;
                for(Node present : openHeap) {
                    if(present.positionEquals(node)) {
                        existing = present;
                        break;
                    }
                }

                if(existing == null) {
                    node.setH(context.getAgent().getHeuristicCalculator().compute(node, context.getDestination()));
                    openHeap.add(node);
                }
                else if(node.getG() < existing.getG()) {
                    openHeap.remove(existing);
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
        return null;
    }
}
