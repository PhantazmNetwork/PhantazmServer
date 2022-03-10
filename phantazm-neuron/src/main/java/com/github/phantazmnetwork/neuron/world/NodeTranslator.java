package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.Nullable;
import com.github.phantazmnetwork.neuron.agent.Agent;

/**
 * <p>This class converts a position and direction — represented by the vectors (x, y, z) and (deltaX, deltaY, deltaZ),
 * respectively — into a vector representative of the movement an {@link Agent} would have to make in order to travel
 * from (x, y, z) to an arbitrary calculated vector {@code dest} which varies depending on the environment and other
 * factors, which may be specific to the agent.</p>
 *
 * <p>NodeTranslator also serves the secondary function of determining which <i>direction</i> vectors are invalid (may
 * not be traversed). If a direction is invalid, {@link NodeTranslator#translate(int, int, int, int, int, int)} will
 * return {@code null}.</p>
 */
@FunctionalInterface
public interface NodeTranslator {
    @Nullable Vec3I translate(int x, int y, int z, int deltaX, int deltaY, int deltaZ);
}
