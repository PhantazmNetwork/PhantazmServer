package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.world.Solid;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link Descriptor} implementation for agents that have width, depth, and height.
 */
public interface PhysicalDescriptor extends Descriptor {
    /**
     * Returns the width of this agent (x-length).
     * @return the width of this agent
     */
    float getWidth();

    /**
     * Returns the depth of this agent (z-length).
     * @return the depth of this agent
     */
    float getDepth();

    /**
     * Returns the height of this agent (y-length).
     * @return the height of this agent
     */
    float getHeight();

    @Override
    default boolean shouldInvalidate(@NotNull Vec3I origin, @NotNull Vec3I update, @NotNull Solid oldSolid,
                                     @NotNull Solid newSolid) {
        for(Vec3I step : stepDirections()) {
            if(overlaps(origin, step, update)) {
                return true;
            }
        }

        return false;
    }

    private boolean overlaps(Vec3I origin, Vec3I step, Vec3I update) {
        double halfWidth = getWidth() / 2;
        double halfDepth = getDepth() / 2;

        double centerX = origin.getX() + 0.5;
        double centerZ = origin.getZ() + 0.5;

        double minX = centerX - halfWidth;
        double minY = origin.getY();
        double minZ = centerZ - halfDepth;

        double maxX = centerX + halfWidth;
        double maxY = origin.getY() + getHeight();
        double maxZ = centerZ + halfDepth;

        double stepX = step.getX();
        double stepY = step.getY();
        double stepZ = step.getZ();

        if(stepX < 0) {
            minX += stepX;
        }
        else if(stepX > 0) {
            maxX += stepX;
        }

        if(stepY < 0) {
            minY += stepY;
        }
        else if(stepY > 0) {
            maxY += stepY;
        }

        if(stepZ < 0) {
            minZ += stepZ;
        }
        else if(stepZ > 0) {
            maxZ += stepZ;
        }

        int iMinX = (int) Math.floor(minX);
        int iMinY = (int) Math.floor(minY);
        int iMinZ = (int) Math.floor(minZ);

        int iMaxX = (int) Math.floor(maxX);
        int iMaxY = (int) Math.floor(maxY);
        int iMaxZ = (int) Math.floor(maxZ);

        int uX = update.getX();
        int uY = update.getY();
        int uZ = update.getZ();

        return uX >= iMinX && uX <= iMaxX && uY >= iMinY && uY <= iMaxY && uZ >= iMinZ && uZ <= iMaxZ;
    }
}
