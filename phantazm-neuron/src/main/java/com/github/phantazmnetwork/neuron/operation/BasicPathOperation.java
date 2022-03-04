package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Calculator;
import com.github.phantazmnetwork.neuron.node.Node;
import com.github.phantazmnetwork.neuron.node.NodeQueue;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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
        this.graph = new Object2ObjectOpenHashMap<>();

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

                Node access = new Node(x, y, z, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, current);
                Node neighbor = graph.get(access);
                if(neighbor == null) {
                    neighbor = access;

                    //compute the heuristic once, on node creation, and never again because its inputs won't change
                    //we're going to compute our g-score a bit later
                    neighbor.setH(calculator.heuristic(neighbor.getX(), neighbor.getY(), neighbor.getZ(),
                            destination.getX(), destination.getY(), destination.getZ()));

                    /*
                    this may seem strange, but it allows us to avoid needing to create and store persistent nodes whose
                    only purpose is to act as a key. done like this, the same object is used as both key and value,
                    which reduces the memory footprint of a large graph significantly
                     */
                    graph.put(neighbor, neighbor);
                }

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
            if(best == null || current.compareTo(best) < 0) {
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
