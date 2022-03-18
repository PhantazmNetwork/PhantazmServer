package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.commons.vector.Vec3IPredicate;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Explorer;
import com.github.phantazmnetwork.neuron.node.Calculator;
import com.github.phantazmnetwork.neuron.node.Node;
import com.github.phantazmnetwork.neuron.node.NodeQueue;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * An A* based {@link PathOperation} implementation. It will only be completed when the destination is found or all
 * nodes are exhausted.
 */
public class BasicPathOperation implements PathOperation {
    private final Vec3I destination;
    private final Vec3IPredicate successPredicate;
    private final Calculator calculator;
    private final Explorer explorer;
    private final NodeQueue openSet;
    private final Map<Node, Node> graph;

    private State state;
    private Node current;
    private Node best;
    private PathResult result;

    public BasicPathOperation(@NotNull Vec3I start, @NotNull Vec3I destination,
                              @NotNull Vec3IPredicate successPredicate, @NotNull Calculator calculator,
                              @NotNull Explorer explorer) {
        this.destination = Objects.requireNonNull(destination, "destination");
        this.successPredicate = Objects.requireNonNull(successPredicate, "successPredicate");
        this.calculator = Objects.requireNonNull(calculator, "calculator");
        this.explorer = Objects.requireNonNull(explorer, "explorer");

        this.openSet = new NodeQueue();
        this.graph = new Object2ObjectRBTreeMap<>();
        this.state = State.IN_PROGRESS;

        Node initial = new Node(start.getX(), start.getY(), start.getZ(), 0, calculator.heuristic(start.getX(), start
                .getY(), start.getZ(), destination.getX(), destination.getY(), destination.getZ()), null);

        //add the first node
        openSet.enqueue(initial);
        graph.put(initial, initial);
    }

    private void complete(State state) {
        this.state = state;

        //clear the graph, we completed this operation
        openSet.clear();
        graph.clear();

        boolean success = state == State.SUCCEEDED;
        result = new BasicResult(success ? current.reverse() : (best == null ? null : best.reverse()), success);
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

            //check if we reached our destination yet
            if(successPredicate.test(current.getX(), current.getY(), current.getZ())) {
                //success state, path was found
                complete(State.SUCCEEDED);
                return;
            }

            for(Vec3I walkVector : explorer.walkVectors(current)) {
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
