package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Calculator;
import com.github.phantazmnetwork.neuron.node.Node;
import com.github.phantazmnetwork.neuron.node.NodeQueue;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BasicPathOperation implements PathOperation {
    private class Result implements PathResult {
        private final Node node;

        private Result(Node node) {
            this.node = node;
        }

        @Override
        public @NotNull PathOperation getOperation() {
            return BasicPathOperation.this;
        }

        @Override
        public @NotNull Node getPath() {
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

    public BasicPathOperation(@NotNull PathContext context) {
        this.context = Objects.requireNonNull(context, "context");

        this.openSet = new NodeQueue();
        this.graph = new Object2ObjectRBTreeMap<>();

        this.state = State.IN_PROGRESS;

        Calculator heuristicCalculator = context.getAgent().getCalculator();
        Vec3I start = context.getStart();
        Vec3I destination = context.getEnd();

        Node initial = new Node(start.getX(), start.getY(), start.getZ(), 0, heuristicCalculator.heuristic(start
                .getX(), start.getY(), start.getZ(), destination.getX(), destination.getY(), destination.getZ()),
                null);

        //add the first node
        openSet.enqueue(initial);
        graph.put(initial, initial);
    }

    private void complete(State state) {
        this.state = state;
        openSet.clear();
        graph.clear();
        result = new Result(state == State.SUCCEEDED ? current.reverse() : best.reverse());
    }

    @Override
    public void step() {
        //don't allow users to keep calling step() after we've finished, they should be polling getState()
        if(state != State.IN_PROGRESS) {
            throw new IllegalStateException("Operation has already finished");
        }

        if(!openSet.isEmpty()) {
            //remove and return the smallest (most promising) node
            current = openSet.dequeue();

            Agent agent = context.getAgent();
            Calculator calculator = agent.getCalculator();
            Vec3I destination = context.getEnd();

            //check if we reached our destination yet
            if(agent.reachedDestination(current.getX(), current.getY(), current.getZ(), destination.getX(), destination
                    .getY(), destination.getZ())) {
                //success state, path was found
                complete(State.SUCCEEDED);
                return;
            }

            for(Vec3I walkVector : agent.getWalker().walkVectors(current.getX(), current.getY(), current.getZ())) {
                //x, y, and z are the coordinates of the "neighbor" node we're trying to explore
                int x = current.getX() + walkVector.getX();
                int y = current.getY() + walkVector.getY();
                int z = current.getZ() + walkVector.getZ();

                /*
                access is either used as a key to retrieve the "actual" node, or it's added to the graph (only after its
                heuristic is calculated). if it's just used as a key, no references to it will remain. this works
                because nodes are considered equal if and only if their positions are the same (equals/hashCode are
                inconsistent with node's natural ordering); therefore, graph must be a hash-based map and not a
                comparison-based one
                 */
                Node access = new Node(x, y, z, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, current);
                Node neighbor = graph.computeIfAbsent(access, key -> {
                    key.setH(calculator.heuristic(key.getX(), key.getY(), key.getZ(), destination.getX(),
                            destination.getY(), destination.getZ()));
                    return key;
                });

                //tentative g-score, we have to check if we've actually got a better score than our previous path
                float g = current.getG() + calculator.distance(current.getX(), current.getY(), current.getZ(),
                        neighbor.getX(), neighbor.getY(), neighbor.getZ());

                //for brand-new nodes, g is equal to Float.POSITIVE_INFINITY, so this will run for sure
                if(g < neighbor.getG()) {
                    //our path to this node is indeed better, update stuff
                    neighbor.setParent(current);
                    neighbor.setG(g);

                    if(neighbor.isOnHeap()) {
                        //if we're already on the heap, call changed to update its position in-place
                        openSet.changed(neighbor);
                    }
                    else {
                        //otherwise, add it to the heap
                        openSet.enqueue(neighbor);
                    }
                }
            }

            //keep track of the nearest node to the goal, in case it's unreachable
            if(best == null || current.getH() < best.getH()) {
                best = current;
            }
        }
        else {
            //we ran through every node and were unable to find the destination
            //we may still have a decent path to get close to it though
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
