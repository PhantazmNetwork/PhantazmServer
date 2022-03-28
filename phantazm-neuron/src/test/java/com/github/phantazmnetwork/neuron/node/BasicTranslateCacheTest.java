package com.github.phantazmnetwork.neuron.node;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BasicTranslateCacheTest {
    private static class TestAgent implements Agent {
        private final int priority;

        private TestAgent(int priority) {
            this.priority = priority;
        }

        @Override
        public boolean hasStartPosition() {
            return true;
        }

        @Override
        public @NotNull Vec3I getStartPosition() {
            return Vec3I.ORIGIN;
        }

        @Override
        public int compareTo(@NotNull Agent o) {
            return Integer.compare(priority, ((TestAgent)o).priority);
        }
    }

    @Test
    void sharedCache() {
        Agent smaller = new TestAgent(0);
        Agent larger = new TestAgent(1);
        Agent largest = new TestAgent(2);

        TranslateCache cache = new BasicTranslateCache();
        cache.offer(larger, 0, 0, 0, 0, 0, 0, Vec3I.ORIGIN);
        assertEquals(cache.forAgent(smaller, 0, 0, 0, 0, 0, 0).getResult(), Vec3I.ORIGIN);
        assertEquals(cache.forAgent(larger, 0, 0, 0, 0, 0, 0).getResult(), Vec3I.ORIGIN);
        assertNull(cache.forAgent(largest, 0, 0, 0, 0, 0, 0).getResult());
    }
}