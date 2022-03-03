package com.github.phantazmnetwork.commons.collection.map;

import com.github.phantazmnetwork.commons.test.TestUtils;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class TreeSpatialMapTest {
    @Test
    void put() {
        SpatialMap<String> map = new TreeSpatialMap<>();
        map.put(0, 0, 3, "3");
        map.put(0, 0, 2, "2");
        map.put(0, 0, 0, "0");
        map.put(0, 0, 1, "1");
    }

    @Test
    void benchmark() {
        TestUtils.comparativeBenchmark(() -> {
                    SpatialMap<String> map = new TreeSpatialMap<>();
                    return s -> {
                        for(int i = 0; i < 5; i++) {
                            for(int j = 0; j < 5; j++) {
                                for(int k = 0; k < 5; k++) {
                                    map.put(i, j, k, "i=" + i + ", j=" + j + ", k=" + k);
                                }
                            }
                        }
                    };
                },
                () -> {
                    SpatialMap<String> map = new HashSpatialMap<>();
                    return s -> {
                        for(int i = 0; i < 5; i++) {
                            for(int j = 0; j < 5; j++) {
                                for(int k = 0; k < 5; k++) {
                                    map.put(i, j, k, "i=" + i + ", j=" + j + ", k=" + k);
                                }
                            }
                        }
                    };
                }, Object::toString, "TreeSpatialMap", "HashSpatialMap", "put",
                1000, 100);
    }
}