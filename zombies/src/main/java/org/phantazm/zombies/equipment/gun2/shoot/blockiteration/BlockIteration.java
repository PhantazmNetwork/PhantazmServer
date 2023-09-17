package org.phantazm.zombies.equipment.gun2.shoot.blockiteration;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface BlockIteration {

    @NotNull Context createContext();

    interface Context {

        boolean isValidEndpoint(@NotNull Point blockLocation, @NotNull Block block);

        boolean acceptRaytracedBlock(@NotNull Vec intersection, @NotNull Block block);

    }

}
