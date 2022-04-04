package com.github.phantazmnetwork.commons.vector;

import com.github.phantazmnetwork.commons.Wrapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CubicVec3IPoolTest {
    private static final List<Integer> POW_2 = List.of(2, 4, 8, 16, 32, 64, 128, 256);

    @Test
    void failFast() {
        for(int i = 0; i <= (POW_2.get(POW_2.size() - 1)); i++) {
            if(!POW_2.contains(i)) {
                int finalI = i;
                assertThrows(IllegalArgumentException.class, () -> new CubicVec3IPool(finalI));
            }
        }
    }

    @Test
    void allPow2Widths() {
        for(int pow2 : POW_2) {
            new CubicVec3IPool(pow2);
        }
    }

    @Test
    void correctValues() {
        Vec3IPool pool = new CubicVec3IPool(8);
        for(int i = -4; i < 4; i++) {
            for(int j = -4; j < 4; j++) {
                for(int k = -4; k < 4; k++) {
                    assertEquals(new BasicVec3I(i, j, k), pool.fromCache(i, j, k));
                }
            }
        }

        Vec3IPool tinyPool = new CubicVec3IPool(2);
        assertEquals(new BasicVec3I(0, 0, 0), tinyPool.fromCache(0, 0, 0));
        assertEquals(new BasicVec3I(-1, -1, -1), tinyPool.fromCache(-1, -1, -1));
    }

    @Test
    void outOfRangeNull() {
        Vec3IPool pool = new CubicVec3IPool(8);
        assertNull(pool.fromCache(-5, -5, -5));
        assertNull(pool.fromCache(4, 4, 4));

        Vec3IPool tinyPool = new CubicVec3IPool(2);
        assertNull(tinyPool.fromCache(0, 0, 1));
        assertNull(tinyPool.fromCache(-2, 0, 0));
    }
}