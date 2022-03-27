package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.vector.Vec3F;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpatialColliderTest {
    private static final Solid BOTTOM_HALF_CUBE = makeSolid(Vec3F.of(0, 0, 0), Vec3F.of(1, 0.5F, 1));

    private static SpatialCollider makeCollider(Map<Vec3I, Solid> collisions) {
        //use spy so we use the VoxelSpace implementation of solidsOverlapping
        //this is to avoid having to duplicate the complex functionality of VoxelSpace.BasicSolidIterator
        Space mockSpace = spy(VoxelSpace.class);
        for(Map.Entry<Vec3I, Solid> entry : collisions.entrySet()) {
            Vec3I pos = entry.getKey();
            when(mockSpace.solidAt(eq(pos.getX()), eq(pos.getY()), eq(pos.getZ()))).thenReturn(entry.getValue());
        }

        return new SpatialCollider(mockSpace);
    }

    private static Solid makeSolid(Vec3F min, Vec3F max) {
        Solid solid = mock(Solid.class);
        when(solid.getMin()).thenReturn(min);
        when(solid.getMax()).thenReturn(max);
        return solid;
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void nullSpaceThrows() {
        assertThrows(NullPointerException.class, () -> new SpatialCollider(null));
    }

    @Nested
    class CubeAgent {
        @Nested
        class FullCubes {
            private static final Solid FULL_CUBE = makeSolid(Vec3F.of(0, 0, 0), Vec3F.of(1, 1, 1));

            @Nested
            class CardinalSolids {
                private static final Map<Vec3I, Solid> FEET = Map.of(Vec3I.of(1, 0, 0), FULL_CUBE, Vec3I.of(-1,
                        0, 0), FULL_CUBE, Vec3I.of(0, 0, 1), FULL_CUBE, Vec3I.of(0, 0, -1), FULL_CUBE);

                private static final Map<Vec3I, Solid> HEAD = Map.of(Vec3I.of(1, 1, 0), FULL_CUBE, Vec3I.of(-1,
                        1, 0), FULL_CUBE, Vec3I.of(0, 1, 1), FULL_CUBE, Vec3I.of(0, 1, -1), FULL_CUBE);

                private static final List<Vec3I> CARDINAL_WALKS = List.of(Vec3I.of(1, 0, 0), Vec3I.of(-1, 0,
                        0), Vec3I.of(0, 0, 1), Vec3I.of(0, 0, -1));

                private static final List<Vec3I> ALL_WALKS = List.of(Vec3I.of(1, 0, 0), Vec3I.of(-1, 0, 0),
                        Vec3I.of(0, 0, 1), Vec3I.of(0, 0, -1), Vec3I.of(1, 0, 1), Vec3I.of(-1,
                                0, -1), Vec3I.of(-1, 0, 1), Vec3I.of(1, 0, -1));

                @Test
                void feet() {
                    SpatialCollider collider = makeCollider(FEET);

                    for(Map.Entry<Vec3I, Solid> entry : FEET.entrySet()) {
                        Vec3I dir = entry.getKey();
                        assertEquals(entry.getValue().getMax().getY(), collider.highestCollisionAlong(0, 0, 0,
                                1, 1, 1, dir.getX(), dir.getY(), dir.getZ()));
                    }
                }

                @Test
                void head() {
                    SpatialCollider collider = makeCollider(HEAD);

                    for(Map.Entry<Vec3I, Solid> entry : HEAD.entrySet()) {
                        Vec3I dir = entry.getKey();
                        assertEquals(Double.NEGATIVE_INFINITY, collider.highestCollisionAlong(0, 0, 0, 1,
                                1, 1, dir.getX(), 0, dir.getZ()));
                    }
                }

                @Test
                void noCollisionWhenOverlap() {
                    SpatialCollider collider = makeCollider(Map.of(Vec3I.ORIGIN, FULL_CUBE));

                    for(Vec3I dir : ALL_WALKS) {
                        assertEquals(Double.NEGATIVE_INFINITY, collider.highestCollisionAlong(0, 0, 0, 1,
                                1, 1, dir.getX(), dir.getY(), dir.getZ()));
                    }
                }

                @Nested
                class Squeeze {
                    private static final Map<Vec3I, Solid> HORIZONTAL = Map.of(Vec3I.of(1, 0, 1), FULL_CUBE,
                            Vec3I.of(-1, 0, -1), FULL_CUBE, Vec3I.of(-1, 0, 1), FULL_CUBE, Vec3I.of(1,
                                    0, -1), FULL_CUBE);

                    private static final Map<Vec3I, Solid> VERTICAL = Map.of(Vec3I.of(1, -1, 0), FULL_CUBE,
                            Vec3I.of(-1, -1, 0), FULL_CUBE, Vec3I.of(0, -1, 1), FULL_CUBE, Vec3I.of(0,
                                    -1, -1), FULL_CUBE, Vec3I.of(1, 1, 0), FULL_CUBE, Vec3I.of(-1, 1,
                                    0), FULL_CUBE, Vec3I.of(0, 1, 1), FULL_CUBE, Vec3I.of(0, 1, -1),
                            FULL_CUBE);

                    @Test
                    void horizontal() {
                        SpatialCollider collider = makeCollider(HORIZONTAL);

                        for(Vec3I dir : CARDINAL_WALKS) {
                            assertEquals(Double.NEGATIVE_INFINITY, collider.highestCollisionAlong(0, 0, 0,
                                    1, 1, 1, dir.getX(), dir.getY(), dir.getZ()));
                        }
                    }

                    @Test
                    void vertical() {
                        SpatialCollider collider = makeCollider(VERTICAL);

                        for(Vec3I dir : ALL_WALKS) {
                            assertEquals(Double.NEGATIVE_INFINITY, collider.highestCollisionAlong(0, 0, 0,
                                    1, 1, 1, dir.getX(), dir.getY(), dir.getZ()));
                        }
                    }
                }
            }
        }
    }
}