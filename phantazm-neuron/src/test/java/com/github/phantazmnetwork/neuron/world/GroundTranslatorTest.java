package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.GroundAgent;
import com.github.phantazmnetwork.neuron.node.TranslateCache;
import com.github.phantazmnetwork.neuron.node.GroundTranslator;
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

        TranslateCache mockCache = mock(TranslateCache.class);
        when(mockCache.forAgent(any(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(TranslateCache.Result.MISS);

        OngoingStubbing<Double> highest = when(mockCollider.highestCollisionAlong(anyDouble(), anyDouble(), anyDouble(),
                anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()));
        for(Double collision : highestCollisions) {
            highest = highest.thenReturn(collision);
        }
        highest.thenReturn(Double.NEGATIVE_INFINITY);

        OngoingStubbing<Double> lowest = when(mockCollider.lowestCollisionAlong(anyDouble(), anyDouble(), anyDouble(),
                anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()));
        for(Double collision : lowestCollisions) {
            lowest = lowest.thenReturn(collision);
        }
        lowest.thenReturn(Double.POSITIVE_INFINITY);

        return new GroundTranslator(mockCache, mockCollider, mockAgent);
    }

    @Nested
    class CubicAgent {
        @Nested
        class Walk {
            @Test
            void walkNoCollision() {
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

        @Nested
        class Jump {
            @Test
            void jumpNoCollision() {
                GroundTranslator translator = mockTranslator(1, 1, 1, 0,
                        Collections.emptyMap(), List.of(1D), Collections.emptyList());
                Vec3I translate = translator.translate(0, 0, 0, 1, 0, 0);
                assertEquals(Vec3I.of(1, 1, 0), translate);
            }

            @Test
            void jumpCollision() {
                GroundTranslator translator = mockTranslator(1, 1, 1, 0,
                        Collections.emptyMap(), List.of(2D),
                        Collections.emptyList());
                assertNull(translator.translate(0, 0, 0, 1, 0, 0));
            }

            @Test
            void jumpHigh() {
                GroundTranslator translator = mockTranslator(1, 1, 2, 0,
                        Collections.emptyMap(), List.of(2D), Collections.emptyList());
                Vec3I translate = translator.translate(0, 0, 0, 1, 0, 0);
                assertEquals(Vec3I.of(1, 2, 0), translate);
            }

            @Test
            void jumpHitHead() {
                GroundTranslator translator = mockTranslator(1, 1, 2, 0,
                        Collections.emptyMap(), List.of(1D), List.of(1D));
                Vec3I translate = translator.translate(0, 0, 0, 1, 0, 0);
                assertNull(translate);
            }

            @Test
            void jumpVeryHigh() {
                GroundTranslator translator = mockTranslator(1, 1, 69, 0,
                        Collections.emptyMap(), List.of(1D, 2D, 3D, 4D, 5D), Collections.emptyList());
                Vec3I translate = translator.translate(0, 0, 0, 1, 0, 0);
                assertEquals(Vec3I.of(1, 5, 0), translate);
            }
        }

        @Nested
        class Fall {
            @Test
            void singleBlock() {
                GroundTranslator translator = mockTranslator(1, 1, 0, 1,
                        Collections.emptyMap(), List.of(Double.NEGATIVE_INFINITY, -1D), Collections.emptyList());
                assertEquals(Vec3I.of(1, -1, 0), translator.translate(0, 0, 0, 1, 0, 0));
            }
        }
    }
}