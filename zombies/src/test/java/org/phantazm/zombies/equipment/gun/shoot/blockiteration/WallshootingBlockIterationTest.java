package org.phantazm.zombies.equipment.gun.shoot.blockiteration;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;
import org.phantazm.zombies.equipment.gun.shoot.wallshooting.WallshootingChecker;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WallshootingBlockIterationTest {

    @Test
    public void testAirDoesNotEndIteration() {
        WallshootingChecker wallshootingChecker = () -> true;
        BlockIteration blockIteration =
                new WallshootingBlockIteration(new WallshootingBlockIteration.Data("", Set.of()), wallshootingChecker);
        BlockIteration.Context context = blockIteration.createContext();

        assertFalse(context.acceptRaytracedBlock(Vec.ZERO, Block.AIR));
    }

    @Test
    public void testStoneIsInitiallyValidEndpoint() {
        WallshootingChecker wallshootingChecker = () -> true;
        BlockIteration blockIteration =
                new WallshootingBlockIteration(new WallshootingBlockIteration.Data("", Set.of()), wallshootingChecker);
        BlockIteration.Context context = blockIteration.createContext();

        assertTrue(context.isValidEndpoint(Vec.ZERO, Block.STONE));
    }

    @Test
    public void testStoneIsInvalidEndpointAfterWallshot() {
        Block passable = Block.BARRIER;
        WallshootingChecker wallshootingChecker = () -> true;
        BlockIteration blockIteration = new WallshootingBlockIteration(
                new WallshootingBlockIteration.Data("", Collections.singleton(passable.key())), wallshootingChecker);
        BlockIteration.Context context = blockIteration.createContext();

        context.acceptRaytracedBlock(Vec.ZERO, passable);

        assertFalse(context.isValidEndpoint(Vec.ZERO, Block.STONE));
    }

}
