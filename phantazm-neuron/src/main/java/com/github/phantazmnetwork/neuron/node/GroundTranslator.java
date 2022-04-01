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

    private final Collider collider;
    private final float width;
    private final float height;
    private final float depth;
    private final float jump;
    private final float fall;

    private final float halfWidth;
    private final float halfDepth;

    /**
     * Creates a new GroundTranslator instance.
     * @param collider the collider used to perform collision checks
     * @param width the width of the agent (x-width)
     * @param height the height of the agent (y-width)
     * @param depth the depth of the agent (z-width)
     * @param jump the maximum jump height of the agent
     * @param fall the maximum distance the agent may fall
     */
    public GroundTranslator(@NotNull Collider collider, float width, float height, float depth, float jump,
                            float fall) {
        this.collider = Objects.requireNonNull(collider, "collider");
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.jump = jump;
        this.fall = fall;

        this.halfWidth = width / 2;
        this.halfDepth = depth / 2;
    }

    @Override
    public @NotNull Vec3I translate(int x, int y, int z, int dX, int dY, int dZ) {
        //center of block at (x, y, z)
        double cX = x + 0.5;
        double cY = collider.heightAt(x, y, z);
        double cZ = z + 0.5;

        //oX, oY, oZ, vX, vY, vZ represent the bounds of the agent standing at (x, y, z) in origin-vector form
        double oX = cX - halfWidth + EPSILON;
        double oY = cY + EPSILON;
        double oZ = cZ - halfDepth + EPSILON;

        double vX = width - EPSILON;
        double vY = height - EPSILON;
        double vZ = depth - EPSILON;

        double highestY = collider.highestCollisionAlong(oX, oY, oZ, vX, vY, vZ, dX, dY, dZ);
        double highestJumpY = cY + jump;

        if(highestY <= highestJumpY) {
            if(highestY == Double.NEGATIVE_INFINITY) { //NEGATIVE_INFINITY means no collision was found
                oX += dX;
                oY += dY;
                oZ += dZ;

                highestY = collider.highestCollisionAlong(oX, oY, oZ, vX, vY, vZ, 0, -fall, 0);
                if(highestY != Double.NEGATIVE_INFINITY) {
                    return Vec3I.of(dX, (int) Math.floor(highestY), dZ);
                }
            }
            else if(jump > 0F) { //we should try to perform a jump, assuming we even can
                double ceiling = Math.min(collider.lowestCollisionAlong(oX, oY, oZ, vX, vY, vZ, 0, jump, 0) -
                        height, highestJumpY);

                while(highestY <= ceiling) {
                    oY = highestY;

                    highestY = collider.highestCollisionAlong(oX, oY, oZ, vX, vY, vZ, dX, dY, dZ);
                    if(highestY == Double.NEGATIVE_INFINITY) { //gap found
                        return Vec3I.of(dX, (int) Math.floor(oY), dZ);
                    }
                }
            }
        }

        return Vec3I.ORIGIN;
    }
}