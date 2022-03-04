package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.neuron.NodeUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NodeTest {
    @Test
    void compare() {
        Node smaller = new Node(0, 0, 0, 0, 0, null);
        Node larger = new Node(1, 1, 1, 1, 0, null);
        Node eq = new Node(2, 2, 2, 0, 0, null);

        assertTrue(smaller.compareTo(larger) < 0);
        assertTrue(larger.compareTo(smaller) > 0);
        assertEquals(0, smaller.compareTo(eq));
    }

    @Test
    void iterate() {
        Node[] nodes = NodeUtils.makeLinkedArray(10);

        int i = 0;
        for(Node node : nodes[0]) {
            assertSame(nodes[i++], node);
        }

        assertSame(i, nodes.length);
    }

    @Test
    void invertSingle() {
        Node node = new Node(0, 0, 0);
        assertSame(node, node.invert());
    }

    @Test
    void invertMany() {
        Node[] nodes = NodeUtils.makeLinkedArray(10);

        Node inverted = nodes[0].invert();
        int i = 0;
        for(Node node : inverted) {
            assertEquals(nodes[nodes.length - (i++ + 1)], node);
        }
    }
}