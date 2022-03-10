package com.github.phantazmnetwork.neuron.world;

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

    //unnecessary local values aid readability here
    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    public @Nullable Vec3I translate(int x, int y, int z, int deltaX, int deltaY, int deltaZ) {
        double cX = x + 0.5;
        double cY = y;
        double cZ = z + 0.5;

        float width = agent.getWidth();
        float height = agent.getHeight();
        float halfWidth = width / 2;

        //agent bounds centered at (x, y, z)
        double minX = cX - halfWidth;
        double minY = y;
        double minZ = cZ - halfWidth;

        double maxX = cX + halfWidth;
        double maxY = y + height;
        double maxZ = cZ + halfWidth;

        //expand in the direction of (deltaX, deltaY, deltaZ)
        if(deltaX < 0) {
            minX += deltaX;
        }
        else {
            maxX += deltaX;
        }

        if(deltaY < 0) {
            minY += deltaY;
        }
        else {
            maxY += deltaY;
        }

        if(deltaZ < 0) {
            minZ += deltaZ;
        }
        else {
            maxZ += deltaZ;
        }

        for(Solid solid : collider.solidsAt(minX, minY, minZ, maxX, maxY, maxZ)) {

        }

        return null;
    }
}
