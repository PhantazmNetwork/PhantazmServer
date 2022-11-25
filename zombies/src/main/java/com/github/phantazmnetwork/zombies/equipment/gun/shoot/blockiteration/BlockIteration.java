package com.github.phantazmnetwork.zombies.equipment.gun.shoot.blockiteration;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * A method of iteration over {@link Point}s.
 * Implementations may choose to stop iterating early and whether block ray tracing should be used.
 */
@FunctionalInterface
public interface BlockIteration {

    @NotNull Context createContext();

    interface Context {

        boolean isValidEndpoint(@NotNull Point blockLocation, @NotNull Block block);

        boolean acceptRaytracedBlock(@NotNull Vec intersection, @NotNull Block block);

    }

}
