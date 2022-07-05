package com.github.phantazmnetwork.commons.vector;

import com.github.phantazmnetwork.commons.InterpolationUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashBigSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class InterpolationUtilsTest {
    @Test
    void straightLine() {
        List<Vec3I> blockIntersections = new ArrayList<>(3);
        InterpolationUtils.interpolateLine(Vec3D.of(0.5, 0.5, 0.5), Vec3D.of(2.5, 0.5, 0.5), vec -> {
            blockIntersections.add(vec.immutable());
            return false;
        });

        assertEquals(3, blockIntersections.size());
        for(int i = 0; i < 3; i++) {
            assertEquals(i, blockIntersections.get(i).getX());
        }
    }

    @Test
    void singleBlock() {
        List<Vec3I> blockIntersections = new ArrayList<>(1);
        InterpolationUtils.interpolateLine(Vec3D.of(0.5, 0.5, 0.5), Vec3D.of(0.6, 0.6, 0.6), vec ->
                blockIntersections.add(vec.immutable()));

        assertEquals(1, blockIntersections.size());
        assertEquals(Vec3I.ORIGIN, blockIntersections.get(0));
    }

    @Test
    void hugeLineNoDuplicates() {
        Set<Vec3I> vecs = new ObjectOpenHashSet<>();
        InterpolationUtils.interpolateLine(Vec3D.of(15.5, 12.54, 87.5), Vec3D.of(10150, 543, 123),
                vec -> {
            assertTrue(vecs.add(vec.immutable()), vec.toString());
            return false;
        });
    }
}