package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.commons.MathUtils;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Explorer;
import com.github.phantazmnetwork.neuron.node.Calculator;
import com.github.phantazmnetwork.neuron.node.Node;
import com.github.phantazmnetwork.neuron.node.NodeQueue;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

/**
 * An A* based {@link PathOperation} implementation. It will only be completed when the destination is found or all
 * nodes are exhausted.
 */
public class BasicPathOperation implements PathOperation {
    private final Vec3I destination;
    private final Predicate<Vec3I> successPredicate;
    private final Calculator calculator;
    private final Explorer explorer;
    private final NodeQueue openSet;
    private final Object2ObjectOpenHashMap<Vec3I, Node> graph;

    private State state;
    private Node best;
    private PathResult result;

    /**
     * Creates a new instance of BasicPathOperation from the given arguments.
     * @param start the position of the starting node
     * @param destination the position of the destination node
     * @param successPredicate the predicate used to determine if the path has completed
     * @param calculator the {@link Calculator} instance used to calculate distance/heuristics
     * @param explorer the {@link Explorer} instance used to expand new nodes
     * @param xOffset the x-offset of the starting node
     * @param zOffset the z-offset of the starting node
     * @throws NullPointerException if any of the arguments are null
     */
    public BasicPathOperation(@NotNull Vec3I start, @NotNull Vec3I destination,
                              @NotNull Predicate<Vec3I> successPredicate, @NotNull Calculator calculator,
                              @NotNull Explorer explorer, float xOffset, float zOffset) {
        this.destination = Objects.requireNonNull(destination, "destination");
        this.successPredicate = Objects.requireNonNull(successPredicate, "successPredicate");
        this.calculator = Objects.requireNonNull(calculator, "calculator");
        this.explorer = Objects.requireNonNull(explorer, "explorer");

        this.openSet = new NodeQueue();
        this.graph = new Object2ObjectOpenHashMap<>();
        this.state = State.IN_PROGRESS;

        Node initial = new Node(start, 0, calculator.heuristic(start, destination), null);

        //y offset will be computed later, set == to 0 for now
        initial.setOffset(xOffset, 0, zOffset);

        //add the first node
        openSet.enqueue(initial);
        graph.put(start, initial);
    }

    private void complete(boolean succeeded, Node node) {
        state = succeeded ? State.SUCCEEDED : State.FAILED;
        result = new PathResult(node.reverse(), graph.size(), succeeded);

        //clear everything and trim to minimize space immediately
        openSet.clear();
        graph.clear();

        graph.trim();
        openSet.trim();

        best = null;
    }

    @Override
    public void step() {
        //don't allow users to keep calling step() after we've finished, they should be polling getState()
        if(isComplete()) {
            throw new IllegalStateException("Operation has already finished");
        }

        if(!openSet.isEmpty()) {
            //remove and return the smallest (most promising) node
            Node current = openSet.dequeue();
            explorer.initializeNode(current);

            //check if we reached our destination yet
            Vec3I currentPos = current.getPosition();
            if(successPredicate.test(currentPos)) {
                //success state, path was found
                complete(true, current);
                return;
            }

            //we haven't reached the destination, expand current
            for(Vec3I walkVector : explorer.expandNode(current)) {
                //x, y, and z are the coordinates of the "neighbor" node we're trying to explore
                int x = currentPos.getX() + walkVector.getX();
                int y = currentPos.getY() + walkVector.getY();
                int z = currentPos.getZ() + walkVector.getZ();

                /*
                the node used as a key here may be stored as a value too, or it may just be used to access a
                previously-existing value. for the purposes of all maps (even comparator-based ones using Node's natural
                ordering), node objects are safe for use as keys because comparison-changing values are final
                 */
                Node neighbor = graph.computeIfAbsent(Vec3I.of(x, y, z), (Vec3I key) -> new Node(key, Float
                        .POSITIVE_INFINITY, calculator.heuristic(key, destination), current));

                Vec3I neighborPos = neighbor.getPosition();
                //tentative g-score, we have to check if we've actually got a better score than our previous path
                float g = current.getG() + calculator.distance(currentPos, neighborPos);

                //for brand-new nodes, neighbor.getG() is equal to Float.POSITIVE_INFINITY, so this will run for sure
                //if however neighbor.getG() is less optimal, we will not explore the node
                if(g < neighbor.getG()) {
                    //our path to this node is indeed better, update stuff
                    neighbor.setParent(current);
                    neighbor.setG(g);
                    openSet.enqueueOrUpdate(neighbor);
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
            complete(false, best);
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
