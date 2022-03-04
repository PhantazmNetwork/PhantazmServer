package com.github.phantazmnetwork.neuron;

import com.github.phantazmnetwork.neuron.operation.Node;

public final class NodeUtils {
    public static Node[] makeLinkedArray(int size) {
        Node[] array = new Node[size];

        Node prev = null;
        for(int i = size - 1; i >= 0; i--) {
            array[i] = prev = new Node(i, i, i, i, i, prev);
        }

        return array;
    }
}
