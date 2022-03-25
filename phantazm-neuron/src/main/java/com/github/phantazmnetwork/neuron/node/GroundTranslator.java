package com.github.phantazmnetwork.neuron.node;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.GroundAgent;
import com.github.phantazmnetwork.neuron.world.Collider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public class GroundTranslator implements NodeTranslator {
    private final Collider collider;
    private final EnvironmentCache cache;
    private final GroundAgent agent;

    public GroundTranslator(@NotNull Collider collider, @NotNull EnvironmentCache cache, @NotNull GroundAgent agent) {
        this.collider = Objects.requireNonNull(collider, "collider");
        this.cache = Objects.requireNonNull(cache, "cache");
        this.agent = Objects.requireNonNull(agent, "agent");
    }

    @Override
    public @Nullable Vec3I translate(int x, int y, int z, int dX, int dY, int dZ) {
        if(cache.isCached(agent, x, y, z, dX, dY, dZ)) {
            return cache.forAgent(agent, x, y, z, dX, dY, dZ);
        }

        //center of block at (x, y, z)
        double cX = x + 0.5;
        double cY = collider.heightAt(x, y, z);
        double cZ = z + 0.5;

        //agent-related variables we'll definitely need
        float width = agent.getWidth();
        float halfWidth = width / 2;
        float height = agent.getHeight();
        float jump = agent.getJumpHeight();

        //oX, oY, oZ, vX, vY, vZ represent the bounds of the agent standing at (x, y, z) in origin-vector form
        double oX = cX - halfWidth;
        double oY = cY;
        double oZ = cZ - halfWidth;

        double highestY = collider.highestCollisionAlong(oX, oY, oZ, width, height, width, dX, dY, dZ);
        double highestJumpY = cY + jump;

        //check to make sure we can make the first jump, if not we'll just return null
        Vec3I result = null;
        if(highestY <= highestJumpY) {
            if(highestY == Double.NEGATIVE_INFINITY) { //NEGATIVE_INFINITY means no collision was found
                oX += dX;
                oY += dY;
                oZ += dZ;

                float fall = agent.getFallTolerance();
                highestY = collider.highestCollisionAlong(oX, oY, oZ, width, height, width, 0, -fall, 0);
                if(highestY != Double.NEGATIVE_INFINITY) {
                    result = Vec3I.of(dX, (int) Math.floor(highestY), dZ);
                }
            }
            else if(jump > 0F) { //we should try to perform a jump, assuming we even can
                double ceiling = Math.min(collider.lowestCollisionAlong(oX, oY, oZ, width, height, width, 0, jump,
                        0) - height, highestJumpY);

                while(highestY <= ceiling) {
                    oY = highestY;

                    highestY = collider.highestCollisionAlong(oX, oY, oZ, width, height, width, dX, dY, dZ);
                    if(highestY == Double.NEGATIVE_INFINITY) { //gap found
                        result = Vec3I.of(dX, (int) Math.floor(oY), dZ);
                        break;
                    }
                }
            }
        }

        cache.offer(agent, x, y, z, dX, dY, dZ, result);
        return result;
    }
}