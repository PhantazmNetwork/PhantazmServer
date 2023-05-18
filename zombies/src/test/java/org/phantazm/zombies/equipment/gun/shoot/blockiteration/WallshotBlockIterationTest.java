package org.phantazm.zombies.equipment.gun.shoot.blockiteration;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WallshotBlockIterationTest {

    @Test
    public void testAirDoesNotEndIteration() {
        BlockIteration blockIteration = new WallshotBlockIteration(new WallshotBlockIteration.Data(List.of()));
        BlockIteration.Context context = blockIteration.createContext();

        assertFalse(context.acceptRaytracedBlock(Vec.ZERO, Block.AIR));
    }

    @Test
    public void testStoneIsInitiallyValidEndpoint() {
        BlockIteration blockIteration = new WallshotBlockIteration(new WallshotBlockIteration.Data(List.of()));
        BlockIteration.Context context = blockIteration.createContext();

        assertTrue(context.isValidEndpoint(Vec.ZERO, Block.STONE));
    }

    @Test
    public void testStoneIsInvalidEndpointAfterWallshot() {
        Block passable = Block.BARRIER;
        BlockIteration blockIteration =
                new WallshotBlockIteration(new WallshotBlockIteration.Data(Collections.singleton(passable.key())));
        BlockIteration.Context context = blockIteration.createContext();

        context.acceptRaytracedBlock(Vec.ZERO, passable);

        assertFalse(context.isValidEndpoint(Vec.ZERO, Block.STONE));
    }

}
