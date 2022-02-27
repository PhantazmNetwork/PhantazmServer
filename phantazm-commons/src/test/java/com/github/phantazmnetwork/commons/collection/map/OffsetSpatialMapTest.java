package com.github.phantazmnetwork.commons.collection.map;

import com.github.phantazmnetwork.commons.collection.map.HashSpatialMap;
import com.github.phantazmnetwork.commons.collection.map.OffsetSpatialMap;
import com.github.phantazmnetwork.commons.collection.map.SpatialMap;
import com.github.phantazmnetwork.commons.test.TestUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Note: will be removed along with {@link OffsetSpatialMap}
 */
class OffsetSpatialMapTest {
    @Nested
    class Put {
        @Test
        void sequential() {
            OffsetSpatialMap<String> chunkSpatialMap = new OffsetSpatialMap<>();

            int width = 20;
            for(int i = 0; i < width; i++) {
                for(int j = 0; j < width; j++) {
                    for(int k = 0; k < width; k++) {
                        chunkSpatialMap.put(i, j, k, "i=" + i + ", j=" + j + ", k=" + k);
                    }
                }
            }

            for(int i = 0; i < width; i++) {
                for(int j = 0; j < width; j++) {
                    for(int k = 0; k < width; k++) {
                        assertEquals("i=" + i + ", j=" + j + ", k=" + k, chunkSpatialMap.get(i, j, k));
                    }
                }
            }
        }
    }

    @Nested
    class Benchmark {
        @Test
        void put() {
            TestUtils.comparativeBenchmark(() -> {
                SpatialMap<String> map = new OffsetSpatialMap<>();
                return (string) -> {
                    for(int i = 0; i < 10; i++) {
                        for(int j = 0; j < 10; j++) {
                            for(int k = 0; k < 10; k++) {
                                map.put(i, j, k, string);
                            }
                        }
                    }
                };
            }, () -> {
                SpatialMap<String> map = new HashSpatialMap<>();
                return (string) -> {
                    for(int i = 0; i < 10; i++) {
                        for(int j = 0; j < 10; j++) {
                            for(int k = 0; k < 10; k++) {
                                map.put(i, j, k, string);
                            }
                        }
                    }
                };
            }, "OffsetSpatialMap", "HashSpatialMap", "put", 100, 1000);
        }
    }
}