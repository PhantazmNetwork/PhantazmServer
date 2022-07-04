package com.github.phantazmnetwork.commons.vector;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LineUtilsTest {
    @Test
    void straightLine() {
        List<Vec3I> blockIntersections = new ArrayList<>(3);
        LineUtils.iterateLine(Vec3D.of(0.5, 0.5, 0.5), Vec3D.of(2.5, 2.5, 2.5), vec ->
                blockIntersections.add(vec.immutable()));

        assertEquals(3, blockIntersections.size());
        for(int i = 0; i < 3; i++) {
            assertEquals(i, blockIntersections.get(i).getX());
        }
    }

    @Test
    void angledLine() {
        List<Vec3I> blockIntersections = new ArrayList<>(2);
        LineUtils.iterateLine(Vec3D.of(0.5, 0.5, 0.5), Vec3D.of(1.5, 1.5, 1.5), vec ->
                blockIntersections.add(vec.immutable()));

        assertEquals(2, blockIntersections.size());
        for(int i = 0; i < 2; i++) {
            Vec3I intersection = blockIntersections.get(i);
            assertEquals(i, intersection.getX());
            assertEquals(i, intersection.getZ());
        }
    }

    @Test
    void singleBlock() {
        List<Vec3I> blockIntersections = new ArrayList<>(1);
        LineUtils.iterateLine(Vec3D.of(0.5, 0.5, 0.5), Vec3D.of(0.6, 0.6, 0.6), vec ->
                blockIntersections.add(vec.immutable()));

        assertEquals(1, blockIntersections.size());
        assertEquals(Vec3I.ORIGIN, blockIntersections.get(0));
    }
}