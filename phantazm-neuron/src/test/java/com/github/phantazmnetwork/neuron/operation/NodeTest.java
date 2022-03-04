package com.github.phantazmnetwork.neuron.operation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NodeTest {
    private static Node[] makeLinkedArray(int size) {
        Node[] array = new Node[size];

        Node prev = null;
        for(int i = size - 1; i >= 0; i--) {
            array[i] = prev = new Node(i, i, i, i, i, prev);
        }

        return array;
    }

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
        Node[] nodes = makeLinkedArray(10);

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
        Node[] nodes = makeLinkedArray(10);

        Node inverted = nodes[0].invert();
        int i = 0;
        for(Node node : inverted) {
            assertEquals(nodes[nodes.length - (i++ + 1)], node);
        }
    }
}