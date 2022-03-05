package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.neuron.world.TerrainCollider;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an {@link Agent} which exists in physical space.
 * @see Agent
 * @see GroundAgent
 */
public interface PhysicalAgent extends Agent {
    @NotNull TerrainCollider getTerrainCollider();
}
