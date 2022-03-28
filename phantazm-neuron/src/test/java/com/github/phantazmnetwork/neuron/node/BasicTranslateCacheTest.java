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
            return false;
        }

        @Override
        public @NotNull Vec3I getStartPosition() {
            return Vec3I.ORIGIN;
        }

        @Override
        public int compareTo(@NotNull Agent o) {
            if(o instanceof TestAgent agent) {
                return Integer.compare(priority, agent.priority);
            }

            return 0;
        }
    }

    @Test
    void removeLikeAgents() {

    }
}