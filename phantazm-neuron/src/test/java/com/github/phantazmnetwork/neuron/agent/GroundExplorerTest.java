package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.ImmutableVec3I;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.world.Collider;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.*;

class GroundExplorerTest {
    private static GroundExplorer makeWalker(Predicate<Vec3I> blocked) {
        Collider mockCollider = Mockito.mock(Collider.class);
        Mockito.when(mockCollider.snap(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt()))
                .thenAnswer(invocation -> {
                    int x = invocation.getArgument(0);
                    int y = invocation.getArgument(1);
                    int z = invocation.getArgument(2);

                    int vX = invocation.getArgument(3);
                    int vY = invocation.getArgument(4);
                    int vZ = invocation.getArgument(5);

                    Vec3I vector = new ImmutableVec3I(x + vX, y + vY, z + vZ);
                    return blocked.test(vector) ? null : vector;
                });

        WalkingAgent mockAgent = Mockito.mock(WalkingAgent.class);
        Mockito.when(mockAgent.getCollider()).thenReturn(mockCollider);
        return new GroundExplorer(mockAgent);
    }

    @Test
    void iteration() {

    }
}