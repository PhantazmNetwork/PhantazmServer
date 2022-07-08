package com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.positional;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Finds entities based on a search {@link Point}.
 */
@FunctionalInterface
public interface PositionalEntityFinder {

    /**
     * Finds entities based on a search {@link Point}.
     * @param instance The {@link Instance} to search in
     * @param start The {@link Point} to search from
     * @return A {@link Collection} of {@link Entity}s found
     */
    @NotNull Collection<Entity> findEntities(@NotNull Instance instance, @NotNull Point start);

}
