package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.node.Calculator;
import com.github.phantazmnetwork.neuron.node.Destination;
import com.github.phantazmnetwork.neuron.node.Node;
import com.github.phantazmnetwork.neuron.node.NodeQueue;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * An A* based {@link PathOperation} implementation. It will only be completed when the destination is found or all
 * nodes are exhausted.
 */
public class BasicPathOperation implements PathOperation {
    @SuppressWarnings("ClassCanBeRecord")
    private static class Result implements PathResult {
        private final Node path;
        private final boolean successful;

        private Result(@Nullable Node path, boolean successful) {
            this.path = path;
            this.successful = successful;
        }

        @Override
        public boolean isSuccessful() {
            return successful;
        }

        @Override
        public Node getPath() {
            return path;
        }
    }

    private final PathContext context;
    private final NodeQueue openSet;
    private final Map<Node, Node> graph;

    private State state;
    private Node current;
    private Node best;
    private PathResult result;

    /**
     * Creates a new BasicPathOperation from the given {@link PathContext}. BasicPathOperation instances may be
     * constructed in a completed state, depending on if the associated {@link Agent} has a valid starting node.
     * @param context the PathContext used to instantiate the operation
     */
    public BasicPathOperation(@NotNull PathContext context) {
        this.context = Objects.requireNonNull(context, "context");

        this.openSet = new NodeQueue();
        this.graph = new Object2ObjectRBTreeMap<>();

        this.state = State.IN_PROGRESS;

        Agent agent = context.getAgent();
        if(agent.hasStartPosition()) {
            Calculator heuristicCalculator = agent.getCalculator();
            Vec3I start = agent.getStartPosition();
            Vec3I destination = context.getDestination();

            Node initial = new Node(start.getX(), start.getY(), start.getZ(), 0, heuristicCalculator.heuristic(start
                    .getX(), start.getY(), start.getZ(), destination.getX(), destination.getY(), destination.getZ()),
                    null);

            //add the first node
            openSet.enqueue(initial);
            graph.put(initial, initial);
        }
        else {
            //immediately terminate if the agent has no starting position, the result will have a null node
            complete(State.FAILED);
        }
    }

    private void complete(State state) {
        this.state = state;
        openSet.clear();
        graph.clear();

        boolean success = state == State.SUCCEEDED;
        result = new Result(success ? current.reverse() : (best == null ? null : best.reverse()), success);
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
            Destination destination = context.getDestination();

            //check if we reached our destination yet
            if(agent.reachedDestination(current.getX(), current.getY(), current.getZ(), destination)) {
                //success state, path was found
                complete(State.SUCCEEDED);
                return;
            }

            for(Vec3I walkVector : agent.getWalker().walkVectors(current)) {
                //x, y, and z are the coordinates of the "neighbor" node we're trying to explore
                int x = current.getX() + walkVector.getX();
                int y = current.getY() + walkVector.getY();
                int z = current.getZ() + walkVector.getZ();

                /*
                the node used as a key here may be stored as a value too, or it may just be used to access a
                previously-existing value. for the purposes of all maps (even comparator-based ones using Node's natural
                ordering), node objects are safe for use as keys because comparison-changing values are final
                 */
                Node neighbor = graph.computeIfAbsent(new Node(x, y, z, Float.POSITIVE_INFINITY,
                        Float.POSITIVE_INFINITY, current), key -> {
                    key.setH(calculator.heuristic(key.getX(), key.getY(), key.getZ(), destination.getX(),
                            destination.getY(), destination.getZ()));
                    return key;
                });

                //tentative g-score, we have to check if we've actually got a better score than our previous path
                float g = current.getG() + calculator.distance(current.getX(), current.getY(), current.getZ(),
                        neighbor.getX(), neighbor.getY(), neighbor.getZ());

                //for brand-new nodes, neighbor.getG() is equal to Float.POSITIVE_INFINITY, so this will run for sure
                //if however neighbor.getG() is less optimal, we will not explore the node
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

            //keep track of the nearest node to the goal (the smallest heuristic), in case it's unreachable
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
    public @NotNull PathResult getResult() {
        if(result == null) {
            throw new IllegalStateException("Operation has not yet completed");
        }

        return result;
    }
}
