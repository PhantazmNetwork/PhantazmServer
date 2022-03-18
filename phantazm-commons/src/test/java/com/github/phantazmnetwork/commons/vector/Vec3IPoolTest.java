package com.github.phantazmnetwork.commons.vector;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("ConstantConditions")
class Vec3IPoolTest {
    @Test
    void powerOf2Width() {
        assertEquals(Vec3IPool.CACHE_WIDTH % 2, 0);
    }

    @Test
    void shiftInRange() {
        assertTrue(Vec3IPool.CACHE_WIDTH > 1);
        assertTrue((Integer.numberOfTrailingZeros(Vec3IPool.CACHE_WIDTH) << 1) < Integer.SIZE);
    }

    @Test
    void expectedValues() {
        int halfWidth = Vec3IPool.CACHE_WIDTH >> 1;
        for(int i = -halfWidth; i < halfWidth; i++) {
            for(int j = -halfWidth; j < halfWidth; j++) {
                for(int k = -halfWidth; k < halfWidth; k++) {
                    assertEquals(new ImmutableVec3I(i, j, k), Vec3IPool.retrieve(i, j, k));
                }
            }
        }
    }

    @Test
    void valuesNullBelowRange() {
        int halfWidthPlus1 = (Vec3IPool.CACHE_WIDTH >> 1) + 1;
        assertNull(Vec3IPool.retrieve(-halfWidthPlus1, 0, 0));
        assertNull(Vec3IPool.retrieve(0, -halfWidthPlus1, 0));
        assertNull(Vec3IPool.retrieve(0, 0, -halfWidthPlus1));
        assertNull(Vec3IPool.retrieve(-halfWidthPlus1, -halfWidthPlus1, 0));
        assertNull(Vec3IPool.retrieve(-halfWidthPlus1, 0, -halfWidthPlus1));
        assertNull(Vec3IPool.retrieve(0, -halfWidthPlus1, -halfWidthPlus1));
        assertNull(Vec3IPool.retrieve(-halfWidthPlus1, -halfWidthPlus1, -halfWidthPlus1));
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Test
    void valuesNullAboveRange() {
        int halfWidth = Vec3IPool.CACHE_WIDTH >> 1;
        assertNull(Vec3IPool.retrieve(halfWidth, 0, 0));
        assertNull(Vec3IPool.retrieve(0, halfWidth, 0));
        assertNull(Vec3IPool.retrieve(0, 0, halfWidth));
        assertNull(Vec3IPool.retrieve(halfWidth, halfWidth, 0));
        assertNull(Vec3IPool.retrieve(halfWidth, 0, halfWidth));
        assertNull(Vec3IPool.retrieve(0, halfWidth, halfWidth));
        assertNull(Vec3IPool.retrieve(halfWidth, halfWidth, halfWidth));
    }
}