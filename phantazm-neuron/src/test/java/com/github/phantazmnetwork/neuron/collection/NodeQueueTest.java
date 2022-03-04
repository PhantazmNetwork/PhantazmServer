package com.github.phantazmnetwork.neuron.collection;

import com.github.phantazmnetwork.neuron.operation.Node;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class NodeQueueTest {
    private static Node[] ordered(int amount) {
        Node[] nodes = new Node[amount];
        for(int i = 0; i < amount; i++) {
            nodes[i] = new Node(i, i, i, i, i, null);
        }

        return nodes;
    }

    @Test
    void shuffledNodes() {
        NodeQueue queue = new NodeQueue();
        Node[] nodes = ordered(1000);
        List<Node> nodesShuffled = new ArrayList<>(Arrays.asList(nodes));
        Collections.shuffle(nodesShuffled, new Random(69420));

        for(Node node : nodesShuffled) {
            queue.enqueue(node);
        }

        int i = 0;
        while(!queue.isEmpty()) {
            assertEquals(0, nodes[i++].compareTo(queue.dequeue()));
        }
    }

    @Test
    void update() {
        NodeQueue queue = new NodeQueue();
        Node node = new Node(0, 0, 0, 0, 0, null);
        Node last = new Node(1, 1, 1, 1, 1, null);
        queue.enqueue(node);
        queue.enqueue(last);

        assertSame(node, queue.first());
        node.setH(69);
        queue.changed(node);
        assertSame(last, queue.first());
        last.setH(420);
        queue.changed(last);
        assertSame(node, queue.first());
    }
}