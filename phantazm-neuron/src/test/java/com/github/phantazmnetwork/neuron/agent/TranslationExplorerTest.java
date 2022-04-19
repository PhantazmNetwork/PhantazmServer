package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.engine.PathCache;
import com.github.phantazmnetwork.neuron.node.Node;
import com.github.phantazmnetwork.neuron.node.NodeTranslator;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TranslationExplorerTest {
    private static TranslationExplorer makeExplorer(Iterable<Vec3I> vectors, Predicate<Vec3I> shouldSkip) {
        NodeTranslator mockTranslator = mock(NodeTranslator.class);
        when(mockTranslator.translate(any(), anyInt(), anyInt(), anyInt())).thenAnswer(invocation -> {
            if(shouldSkip.test(((Node)invocation.getArgument(0)).getPosition())) {
                return Vec3I.ORIGIN;
            }

            return Vec3I.of(invocation.getArgument(1), invocation.getArgument(2), invocation
                    .getArgument(3));
        });

        return new TranslationExplorer(null, "", mockTranslator, vectors);
    }

    private static void assertIteratorSameOrder(Iterator<?> expected, Iterator<?> actual) {
        if(expected == actual) {
            return;
        }

        while(true) {
            boolean expectedHasNext = expected.hasNext();
            boolean actualHasNext = actual.hasNext();

            assertFalse((expectedHasNext && !actualHasNext) || (!expectedHasNext && actualHasNext));

            if(expectedHasNext) {
                assertEquals(expected.next(), actual.next());
            }
            else {
                break;
            }
        }
    }

    @Test
    void visitsExpected() {
        List<Vec3I> walks = List.of(Vec3I.of(1, 0, 0));
        TranslationExplorer explorer = makeExplorer(walks, vec3I -> false);

        Iterable<Vec3I> vecs = explorer.expandNode(new Node(Vec3I.ORIGIN, 0, 0, null));
        assertIteratorSameOrder(vecs.iterator(), walks.iterator());
    }

    @Test
    void emptyWhenNull() {
        List<Vec3I> walks = List.of(Vec3I.of(1, 0, 0));
        TranslationExplorer explorer = makeExplorer(walks, vec3I -> true);
        Iterable<Vec3I> vecs = explorer.expandNode(new Node(Vec3I.ORIGIN, 0, 0, null));
        assertIteratorSameOrder(vecs.iterator(), Collections.emptyIterator());
    }
}