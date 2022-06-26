package com.github.phantazmnetwork.commons.vector;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Region3ITest {
    @Nested
    class RegionIterator {
        @Test
        void singleCount() {
            Region3I region = new BasicRegion3I(Vec3I.ORIGIN, Vec3I.of(1, 1, 1));

            int i = 0;
            for(Vec3I vec : region) {
                assertEquals(Vec3I.ORIGIN, vec);
                i++;
            }

            assertEquals(1, i);
        }

        @Test
        void largerCount() {
            Vec3I[] vecs = new Vec3I[16];
            for(int i = 0; i < vecs.length; i++) {
                vecs[i] = Vec3I.of(i, i, i);
            }

            for(Vec3I size : vecs) {
                Region3I region = new BasicRegion3I(Vec3I.ORIGIN, size);
                int count = 0;
                for(Vec3I ignored : region) {
                    count++;
                }

                assertEquals(size.getX() * size.getY() * size.getZ(), count);
            }
        }
    }
}