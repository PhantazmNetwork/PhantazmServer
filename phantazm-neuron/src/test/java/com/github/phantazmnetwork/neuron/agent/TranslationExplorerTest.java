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
    private static TranslationExplorer makeExplorer(Iterable<Vec3I> vectors, Predicate<Vec3I> shouldSkip,
                                                    Function<Vec3I, Vec3I> transform) {
        NodeTranslator mockTranslator = mock(NodeTranslator.class);
        when(mockTranslator.translate(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenAnswer(invocation
                -> {
            Vec3I vec3I = Vec3I.of(invocation.getArgument(3), invocation.getArgument(4), invocation
                    .getArgument(5));

            if(shouldSkip.test(vec3I)) {
                return Vec3I.ORIGIN;
            }

            return transform.apply(vec3I);
        });

        PathCache mockContext = mock(PathCache.class);
        when(mockContext.getSteps(any(), any())).thenReturn(Optional.empty());
        when(mockContext.watchSteps(any(), any(), any())).thenAnswer(invocation -> invocation.getArgument(2));

        return new TranslationExplorer(mockContext, "", mockTranslator, vectors);
    }

    private static void assertIteratorSameOrder(Iterator<?> expected, Iterator<?> actual) {
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
        TranslationExplorer explorer = makeExplorer(walks, vec3I -> false, vec3I -> vec3I);

        Iterable<? extends Vec3I> vecs = explorer.walkVectors(new Node(Vec3I.ORIGIN, 0, 0, null));
        assertIteratorSameOrder(vecs.iterator(), walks.iterator());
    }

    @Test
    void emptyWhenNull() {
        List<Vec3I> walks = List.of(Vec3I.of(1, 0, 0));
        TranslationExplorer explorer = makeExplorer(walks, vec3I -> true, vec3I -> vec3I);
        Iterable<? extends Vec3I> vecs = explorer.walkVectors(new Node(Vec3I.ORIGIN, 0, 0, null));
        assertIteratorSameOrder(vecs.iterator(), Collections.emptyIterator());
    }

    @Test
    void transformApplies() {
        List<Vec3I> walks = List.of(Vec3I.of(1, 0, 0), Vec3I.of(2, 0, 0), Vec3I.of(3, 0, 0));
        List<Vec3I> transformed = List.of(Vec3I.of(2, 0, 0), Vec3I.of(3, 0, 0), Vec3I.of(4, 0, 0));
        TranslationExplorer explorer = makeExplorer(walks, vec3I -> false, vec3I -> Vec3I.of(vec3I.getX() + 1, 0,
                0));
        Iterable<? extends Vec3I> vecs = explorer.walkVectors(new Node(Vec3I.ORIGIN, 0, 0, null));
        assertIteratorSameOrder(vecs.iterator(), transformed.iterator());
    }
}