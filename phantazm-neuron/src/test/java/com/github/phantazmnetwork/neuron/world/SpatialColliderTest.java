package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpatialColliderTest {
    private static SpatialCollider makeCollider(Map<Vec3I, Solid> collisions) {
        Space mockSpace = mock(Space.class);
        for(Map.Entry<Vec3I, Solid> entry : collisions.entrySet()) {
            Vec3I pos = entry.getKey();
            Solid solid = entry.getValue();
            when(mockSpace.solidAt(pos.getX(), pos.getY(), pos.getZ())).thenReturn(solid);
        }

        return new SpatialCollider(mockSpace);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void nullSpaceThrows() {
        assertThrows(NullPointerException.class, () -> new SpatialCollider(null));
    }
}