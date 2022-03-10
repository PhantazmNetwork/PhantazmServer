package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.neuron.node.Node;
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
        Node x = new Node(1, 0, 0, 0, 0, null);
        Node y = new Node(0, 1, 0, 0, 0, null);
        Node z = new Node(0, 0, 1, 0, 0, null);

        assertTrue(x.compareTo(y) > 0);
        assertTrue(y.compareTo(x) < 0);

        assertTrue(y.compareTo(z) > 0);
        assertTrue(z.compareTo(y) < 0);

        assertTrue(x.compareTo(z) > 0);
        assertTrue(z.compareTo(x) < 0);

        assertEquals(new Node(1, 0, 0, 69, 69, z), x);
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
        Node node = new Node(0, 0, 0, 0, 0, null);
        assertSame(node, node.reverse());
    }

    @Test
    void invertMany() {
        Node[] nodes = makeLinkedArray(10);

        Node inverted = nodes[0].reverse();
        int i = 0;
        for(Node node : inverted) {
            assertEquals(nodes[nodes.length - (++i)], node);
        }

        assertEquals(nodes.length, i);
    }
}