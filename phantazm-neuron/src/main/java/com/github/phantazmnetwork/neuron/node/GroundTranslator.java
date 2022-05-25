package com.github.phantazmnetwork.neuron.node;

import com.github.phantazmnetwork.commons.MathUtils;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.GroundDescriptor;
import com.github.phantazmnetwork.neuron.world.Collider;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A ground-based NodeTranslator that takes gravity into account and performs collision checks.
 */
public class GroundTranslator implements NodeTranslator {
    private static final double EPSILON = 1E-5;
    private static final double DOUBLE_EPSILON = EPSILON * 2;
    private static final double HALF_BLOCK = 0.5;

    private final Collider collider;
    private final GroundDescriptor descriptor;

    private final float halfWidth;
    private final float halfDepth;

    /**
     * Creates a new instance of GroundTranslator from the given {@link Collider} (for collision checking) and
     * {@link GroundDescriptor} describing an agent's characteristics.
     * @param collider the collider used to perform collision checking
     * @param descriptor the descriptor describing the characteristics of a gravity-bound agent
     */
    public GroundTranslator(@NotNull Collider collider, @NotNull GroundDescriptor descriptor) {
        this.collider = Objects.requireNonNull(collider, "collider");
        this.descriptor = descriptor;

        this.halfWidth = descriptor.getWidth() / 2;
        this.halfDepth = descriptor.getDepth() / 2;
    }

    @Override
    public @NotNull Vec3I translate(@NotNull Node node, int dX, int dY, int dZ) {
        Vec3I nodePosition = node.getPosition();

        double x = nodePosition.getX() + node.getXOffset();
        double y = nodePosition.getY() + node.getYOffset();
        double z = nodePosition.getZ() + node.getZOffset();

        double nX = nodePosition.getX() + dX + HALF_BLOCK;
        double nZ = nodePosition.getZ() + dZ + HALF_BLOCK;

        double adX = nX - x;
        double adZ = nZ - z;

        float height = descriptor.getHeight();
        float jump = descriptor.getJumpHeight();

        //oX, oY, oZ, vX, vY, vZ represent the bounds of the agent standing at (x, y, z) in origin-vector form
        double oX = x - halfWidth + EPSILON;
        double oY = y + EPSILON;
        double oZ = z - halfDepth + EPSILON;

        double vX = descriptor.getWidth() - DOUBLE_EPSILON;
        double vY = height - DOUBLE_EPSILON;
        double vZ = descriptor.getDepth() - DOUBLE_EPSILON;

        double highestY = collider.highestCollisionAlong(oX, oY, oZ, vX, vY, vZ, adX, 0, adZ);
        double highestJumpY = y + jump;

        if(highestY <= highestJumpY) {
            if(highestY == Double.NEGATIVE_INFINITY) { //NEGATIVE_INFINITY means no collision was found
                oX += adX;
                oZ += adZ;

                highestY = collider.highestCollisionAlong(oX, oY, oZ, vX, vY, vZ, 0, -descriptor.getFallTolerance(),
                        0);
                if(highestY != Double.NEGATIVE_INFINITY) {
                    return Vec3I.of(dX, (int) Math.floor(highestY) - (int) Math.floor(y), dZ);
                }
            }
            else if(jump > 0F) { //we should try to perform a jump, assuming we even can
                //disable diagonal movement if we have to jump, this avoids a lot of jank and means we don't have to
                //perform 2 collision checks for every possible jump
                if(dX * dZ != 0) {
                    return Vec3I.ORIGIN;
                }

                double ceiling = Math.min(collider.lowestCollisionAlong(oX, oY, oZ, vX, vY, vZ, 0, jump, 0) -
                        height, highestJumpY);

                while(highestY <= ceiling) {
                    oY = highestY + EPSILON;

                    highestY = collider.highestCollisionAlong(oX, oY, oZ, vX, vY, vZ, adX, 0, adZ);
                    if(highestY == Double.NEGATIVE_INFINITY) { //gap found
                        return Vec3I.of(dX, (int) Math.floor(oY - y), dZ);
                    }
                }
            }
        }

        return Vec3I.ORIGIN;
    }

    @Override
    public void initializeNode(@NotNull Node node) {
        node.setOffset(node.getXOffset(), (float) MathUtils.floorOffset(collider.heightAt(node.getPosition())), node
                .getZOffset());
    }
}