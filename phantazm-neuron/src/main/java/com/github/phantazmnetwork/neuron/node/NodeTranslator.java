package com.github.phantazmnetwork.neuron.node;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.github.phantazmnetwork.neuron.agent.Agent;

/**
 * <p>This class converts a position and direction — represented by the vectors (x, y, z) and (deltaX, deltaY, deltaZ),
 * respectively — into a vector representative of the movement an {@link Agent} would have to make in order to travel
 * from (x, y, z) to an arbitrary calculated vector {@code dest} which varies depending on the environment and other
 * factors, which may be specific to the agent.</p>
 *
 * <p>NodeTranslator also serves the secondary function of determining which <i>direction</i> vectors are invalid (may
 * not be traversed). If a direction is invalid, {@link NodeTranslator#translate(Node, int, int, int)} will return the
 * origin vector {@link Vec3I#ORIGIN}.</p>
 */
public interface NodeTranslator {
    /**
     * Determines (adjusts) the given node by the delta vector (deltaX, deltaY, deltaZ), typically according to
     * environmental factors. A typical use case would be "snapping" the vector pointing to the location of a new node
     * such that it is on top of a solid. If this movement attempt is not valid, this method will return the origin
     * vector.
     * @param node the node to translate
     * @param deltaX the x-component of the attempted movement vector
     * @param deltaY the y-component of the attempted movement vector
     * @param deltaZ the z-component of the attempted movement vector
     * @return the adjusted delta vector, or {@link Vec3I#ORIGIN} if it's not possible to move this direction
     */
    @NotNull Vec3I translate(@NotNull Node node, int deltaX, int deltaY, int deltaZ);

    /**
     * Computes the height offset value for this node. This is necessary to call for environments that have available,
     * valid places for agents to stand whose heights are not integers. If this is the case, this method should always
     * be called once for each node, before calling {@link NodeTranslator#translate(Node, int, int, int)}.
     * @param node the node to compute the offset value for
     */
    void computeOffset(@NotNull Node node);
}
