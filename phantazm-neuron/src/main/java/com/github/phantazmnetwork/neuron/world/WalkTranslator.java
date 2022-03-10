package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.vector.ImmutableVec3I;
import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.GroundAgent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class WalkTranslator implements NodeTranslator {
    private final Collider collider;
    private final GroundAgent agent;

    public WalkTranslator(@NotNull Collider collider, @NotNull GroundAgent agent) {
        this.collider = Objects.requireNonNull(collider, "collider");
        this.agent = Objects.requireNonNull(agent, "agent");
    }

    @Override
    public @Nullable Vec3I translate(int x, int y, int z, int deltaX, int deltaY, int deltaZ) {
        float width = agent.getWidth();
        float height = agent.getHeight();

        double minX, minY, minZ, maxX, maxY, maxZ;
        if(deltaX < 0) {
            minX = x + deltaX;
            maxX = x;
        }
        else if(deltaX > 0) {
            minX = x + width;
            maxX = x + deltaX;
        }
        else {
            minX = x;
            maxX = x + width;
        }

        if(deltaY < 0) {
            minY = y + deltaY;
            maxY = y;
        }
        else if(deltaY > 0) {
            minY = y + height;
            maxY = y + deltaY;
        }
        else {
            minY = y;
            maxY = y + height;
        }

        if(deltaZ < 0) {
            minZ = z + deltaZ;
            maxZ = z;
        }
        else if(deltaZ > 0) {
            minZ = z + width;
            maxZ = z + deltaZ;
        }
        else {
            minZ = z;
            maxZ = z + width;
        }

        Solid highest = collider.findHighest(minX, minY, minZ, maxX, maxY, maxZ);
        if(highest != null) {
            float jumpHeight = agent.getJumpHeight();

            Solid finalSolid = null;
            while(highest != null && (highest.getY() + highest.maxY() - y) < jumpHeight) {
                finalSolid = highest;

                minY += height;
                maxY += height;
                highest = collider.findHighest(minX, minY, minZ, maxX, maxY, maxZ);
            }

            if(highest == null && !collider.collidesAt(x, y + height, z, x + width, maxY, z + width)) {
                return finalSolid;
            }
        }
        else {
            float fallTolerance = agent.getFallTolerance();

            while(highest == null && y - minY < fallTolerance) {
                minY -= height;
                maxY -= height;
                highest = collider.findHighest(minX, minY, minZ, maxX, maxY, maxZ);
            }

            if(highest != null) {

            }
        }

        return null;
    }
}
