package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.GroundAgent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;

public class WalkTranslator implements NodeTranslator {
    private final Collider collider;
    private final GroundAgent agent;

    public WalkTranslator(@NotNull Collider collider, @NotNull GroundAgent agent) {
        this.collider = Objects.requireNonNull(collider, "collider");
        this.agent = Objects.requireNonNull(agent, "agent");
    }

    //suppressed because some unnecessary local variables improve readability
    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    public @Nullable Vec3I translate(int x, int y, int z, int deltaX, int deltaY, int deltaZ) {
        //center of block at (x, y, z)
        double cX = x + 0.5;
        double cY = y;
        double cZ = z + 0.5;

        //agent variables we'll need
        float width = agent.getWidth();
        float halfWidth = width / 2;
        float height = agent.getHeight();
        float jumpHeight = agent.getJumpHeight();
        float fallTolerance = agent.getFallTolerance();

        //minX, minY, minZ, maxX, maxY, maxZ represent the bounds of the agent
        double minX = cX - halfWidth;
        double minY = cY;
        double minZ = cZ - halfWidth;

        double maxX = cX + halfWidth;
        double maxY = cY + height;
        double maxZ = cZ + halfWidth;

        Iterator<? extends Solid> collisions = collider.collisionsMovingAlong(minX, minY, minZ, maxX, maxY +
                        jumpHeight, maxZ, deltaX, deltaY, deltaZ, Collider.Order.YXZ).iterator();

        if(collisions.hasNext()) {
            do {
                Solid hit = collisions.next();
            }
            while (collisions.hasNext());
        }

        return null;
    }

    /* shamelessly copying spigot's code for reference
    private boolean overlaps(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return this.minX < maxX && this.maxX > minX
                && this.minY < maxY && this.maxY > minY
                && this.minZ < maxZ && this.maxZ > minZ;
    }
     */
}
