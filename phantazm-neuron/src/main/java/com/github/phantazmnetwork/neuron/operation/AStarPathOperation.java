package com.github.phantazmnetwork.neuron.operation;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class AStarPathOperation implements PathOperation {
    private Node currentNode;
    private final Node goalNode;
    private final Set<Node> visited;
    private final Queue<Node> openSet;
    private final NodeProvider nodeProvider;

    public AStarPathOperation(Node startingNode, Node goalNode, NodeProvider provider) {
        this.visited = new HashSet<>();
        this.openSet = new PriorityQueue<>(32);
        this.openSet.add(startingNode);
        this.goalNode = goalNode;
        this.nodeProvider = provider;
    }

    @Override
    public void step() {
        if(!openSet.isEmpty()) {
            currentNode = openSet.remove();
            if(currentNode.equals(goalNode)) {

            }
            else {
                Node[] nodes = nodeProvider.getNodes(currentNode.getX(), currentNode.getY(), currentNode.getZ());
                for(Node node : nodes) {

                }
            }
        }
    }
}
