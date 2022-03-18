package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.GroundAgent;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.OngoingStubbing;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GroundTranslatorTest {
    private static GroundTranslator mockTranslator(float agentHeight, float agentWidth, float jumpHeight,
                                                   float fallTolerance, Map<Vec3I, Double> heightMap,
                                                   Iterable<Double> highestCollisions,
                                                   Iterable<Double> lowestCollisions) {
        GroundAgent mockAgent = mock(GroundAgent.class);
        when(mockAgent.getHeight()).thenReturn(agentHeight);
        when(mockAgent.getWidth()).thenReturn(agentWidth);
        when(mockAgent.getJumpHeight()).thenReturn(jumpHeight);
        when(mockAgent.getFallTolerance()).thenReturn(fallTolerance);

        Collider mockCollider = mock(Collider.class);
        when(mockCollider.heightAt(anyInt(), anyInt(), anyInt())).thenAnswer(invocation -> {
            Vec3I targetVec = Vec3I.of(invocation.getArgument(0), invocation.getArgument(1),
                    invocation.getArgument(2));
            if(heightMap.containsKey(targetVec)) {
                return heightMap.get(targetVec);
            }

            Integer arg = invocation.getArgument(1);
            return arg.doubleValue();
        });

        OngoingStubbing<Double> highest = when(mockCollider.highestCollisionAlong(anyDouble(), anyDouble(), anyDouble(),
                anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()));
        boolean highestIterated = false;
        for(Double collision : highestCollisions) {
            highest = highest.thenReturn(collision);
            highestIterated = true;
        }

        if(!highestIterated) {
            highest.thenReturn(Double.NEGATIVE_INFINITY);
        }

        OngoingStubbing<Double> lowest = when(mockCollider.lowestCollisionAlong(anyDouble(), anyDouble(), anyDouble(),
                anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()));
        boolean lowestIterated = false;
        for(Double collision : lowestCollisions) {
            lowest = lowest.thenReturn(collision);
            lowestIterated = true;
        }

        if(!lowestIterated) {
            lowest.thenReturn(Double.POSITIVE_INFINITY);
        }

        return new GroundTranslator(mockCollider, mockAgent);
    }

    @Nested
    class CubicAgent {
        @Nested
        class NoJumpOrFall {
            @Test
            void walk() {
                GroundTranslator translator = mockTranslator(1, 1, 0, 0,
                        Collections.emptyMap(), List.of(Double.NEGATIVE_INFINITY, 0D), Collections.emptyList());
                assertEquals(Vec3I.of(1, 0, 0), translator.translate(0, 0, 0, 1, 0, 0));
            }

            @Test
            void walkCollision() {
                GroundTranslator translator = mockTranslator(1, 1, 0, 0,
                        Collections.emptyMap(), List.of(1D), Collections.emptyList());
                assertNull(translator.translate(0, 0, 0, 1, 0, 0));
            }
        }

        @Test
        void jump() {
            GroundTranslator translator = mockTranslator(1, 1, 0, 0,
                    Collections.emptyMap(), List.of(1D), Collections.emptyList());
        }
    }
}