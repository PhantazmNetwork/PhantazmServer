package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.world.Solid;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A {@link Descriptor} for agents that are subject to gravity and can jump or fall.
 */
public interface GroundDescriptor extends PhysicalDescriptor {
    /**
     * The default value returned by {@link GroundDescriptor#stepDirections()}.
     */
    Collection<? extends Vec3I> DEFAULT_WALK_DIRECTIONS = List.of(
            Vec3I.of(1, 0, 0),
            Vec3I.of(0, 0, 1),
            Vec3I.of(-1, 0, 0),
            Vec3I.of(0, 0, -1),

            Vec3I.of(1, 0, 1),
            Vec3I.of(-1, 0, 1),
            Vec3I.of(1, 0, -1),
            Vec3I.of(-1, 0, -1)
    );

    /**
     * Returns the jump height for this agent.
     * @return the jump height for this agent
     */
    float getJumpHeight();

    /**
     * Returns the fall tolerance for this agent. This is the maximum value beyond which agents will no longer be able
     * to pathfind down a vertical drop.
     * @return the fall tolerance for this agent
     */
    float getFallTolerance();

    @Override
    default @NotNull Collection<? extends Vec3I> stepDirections() {
        return DEFAULT_WALK_DIRECTIONS;
    }

    @Override
    default boolean shouldInvalidate(@NotNull Iterable<? extends Vec3I> cached, @NotNull Vec3I origin,
                                     @NotNull Vec3I update, @NotNull Solid oldSolid, @NotNull Solid newSolid) {
        if(oldSolid.equals(newSolid)) {
            //don't invalidate if no change occurred
            return false;
        }

        List<Vec3I> steps = new ArrayList<>(stepDirections());
        for(Vec3I step : cached) { //check cached steps first
            if(overlaps(origin, step, update)) {
                return true;
            }

            steps.removeIf(vector -> vector.getX() == (int) Math.signum(step.getX()) && vector.getY() == (int) Math
                    .signum(step.getY()) && vector.getZ() == (int) Math.signum(step.getZ()));
        }

        for(Vec3I step : steps) {
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
        double minY = origin.getY() - 1; //update if block below the agent changes
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
