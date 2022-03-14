package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.vector.ImmutableVec3I;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.GroundAgent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public class BasicWalkTranslator implements NodeTranslator {
    private final Collider collider;
    private final GroundAgent agent;

    public BasicWalkTranslator(@NotNull Collider collider, @NotNull GroundAgent agent) {
        this.collider = Objects.requireNonNull(collider, "collider");
        this.agent = Objects.requireNonNull(agent, "agent");
    }

    @Override
    public @Nullable Vec3I translate(int x, int y, int z, int deltaX, int deltaY, int deltaZ) {
        //center of block at (x, y, z)
        double cX = x + 0.5;
        double cY = collider.heightAt(x, y, z);
        double cZ = z + 0.5;

        //agent-related variables we'll definitely need
        float width = agent.getWidth();
        float halfWidth = width / 2;
        float height = agent.getHeight();
        float jumpHeight = agent.getJumpHeight();

        //minX, minY, minZ, maxX, maxY, maxZ represent the bounds of the agent standing at (x, y, z)
        double minX = cX - halfWidth;
        double minY = cY;
        double minZ = cZ - halfWidth;

        double maxX = cX + halfWidth;
        double maxY = cY + height;
        double maxZ = cZ + halfWidth;

        //try to move by (deltaX, 0, deltaZ)
        double highestY = collider.highestCollisionAlong(minX, minY, minZ, maxX, maxY, maxZ, deltaX, 0, deltaZ);
        double highestJumpY = cY + jumpHeight;

        //check to make sure we can make the first jump, if not we'll just return null
        if(highestY <= highestJumpY) {
            if(highestY == Double.NEGATIVE_INFINITY) { //NEGATIVE_INFINITY means no collision was found
                float fallTolerance = agent.getFallTolerance();

                maxY = minY;
                minY -= fallTolerance;

                minX += deltaX;
                minZ += deltaZ;

                maxX += deltaX;
                maxZ += deltaZ;

                highestY = collider.highestCollisionAt(minX, minY, minZ, maxX, maxY, maxZ);
                if(highestY != Double.NEGATIVE_INFINITY) {
                    return new ImmutableVec3I(deltaX, (int)Math.floor(highestY), deltaZ);
                }
            }
            else { //we should try to perform a jump
                double ceiling = collider.smallestCollisionAlong(minX, minY, minZ, maxX, maxY, maxZ, 0,
                        jumpHeight, 0) - height;

                while(highestY < highestJumpY && highestY <= ceiling) {
                    minY = highestY;
                    maxY = highestY + height;

                    highestY = collider.highestCollisionAlong(minX, minY, minZ, maxX, maxY, maxZ, deltaX, 0,
                            deltaZ);

                    if(highestY == Double.NEGATIVE_INFINITY) { //gap found
                        return new ImmutableVec3I(deltaX, (int)Math.floor(minY), deltaZ);
                    }
                }
            }
        }

        return null;
    }
}
