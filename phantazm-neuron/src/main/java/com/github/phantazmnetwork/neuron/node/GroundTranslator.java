package com.github.phantazmnetwork.neuron.node;

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

    private final Collider collider;
    private final GroundDescriptor descriptor;

    private final float halfWidth;
    private final float halfDepth;

    public GroundTranslator(@NotNull Collider collider, @NotNull GroundDescriptor descriptor) {
        this.collider = Objects.requireNonNull(collider, "collider");
        this.descriptor = descriptor;

        this.halfWidth = descriptor.getWidth() / 2;
        this.halfDepth = descriptor.getDepth() / 2;
    }

    @Override
    public @NotNull Vec3I translate(@NotNull Node node, int dX, int dY, int dZ) {
        Vec3I nodePosition = node.getPosition();
        int x = nodePosition.getX();
        int y = nodePosition.getY();
        int z = nodePosition.getZ();

        //center of block at (x, y, z)
        double cX = x + 0.5;
        double cY = y + node.getHeightOffset();
        double cZ = z + 0.5;

        //oX, oY, oZ, vX, vY, vZ represent the bounds of the agent standing at (x, y, z) in origin-vector form
        double oX = cX - halfWidth + EPSILON;
        double oY = cY + EPSILON;
        double oZ = cZ - halfDepth + EPSILON;

        float height = descriptor.getHeight();
        float jump = descriptor.getJumpHeight();

        double vX = descriptor.getWidth() - DOUBLE_EPSILON;
        double vY = height - DOUBLE_EPSILON;
        double vZ = descriptor.getDepth() - DOUBLE_EPSILON;

        double highestY = collider.highestCollisionAlong(oX, oY, oZ, vX, vY, vZ, dX, dY, dZ);
        double highestJumpY = cY + jump;

        if(highestY <= highestJumpY) {
            if(highestY == Double.NEGATIVE_INFINITY) { //NEGATIVE_INFINITY means no collision was found
                oX += dX;
                oY += dY;
                oZ += dZ;

                highestY = collider.highestCollisionAlong(oX, oY, oZ, vX, vY, vZ, 0, -descriptor.getFallTolerance(),
                        0);
                if(highestY != Double.NEGATIVE_INFINITY) {
                    return Vec3I.of(dX, (int) Math.floor(highestY - y), dZ);
                }
            }
            else if(jump > 0F) { //we should try to perform a jump, assuming we even can
                double ceiling = Math.min(collider.lowestCollisionAlong(oX, oY, oZ, vX, vY, vZ, 0, jump, 0) -
                        height, highestJumpY);

                //disable diagonal movement if we have to jump, this avoids a lot of jank and means we don't have to
                //perform 2 collision checks for every possible jump
                if(dX * dZ != 0) {
                    return Vec3I.ORIGIN;
                }

                while(highestY <= ceiling) {
                    oY = highestY + EPSILON;

                    highestY = collider.highestCollisionAlong(oX, oY, oZ, vX, vY, vZ, dX, dY, dZ);
                    if(highestY == Double.NEGATIVE_INFINITY) { //gap found
                        return Vec3I.of(dX, (int) Math.floor(oY - y), dZ);
                    }
                }
            }
        }

        return Vec3I.ORIGIN;
    }

    @Override
    public void computeOffset(@NotNull Node node) {
        double height = collider.heightAt(node.getPosition());
        node.setHeightOffset((float) (height - Math.floor(height)));
    }
}