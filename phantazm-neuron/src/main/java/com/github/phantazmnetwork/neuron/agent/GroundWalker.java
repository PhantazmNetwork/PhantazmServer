package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.world.Bounds;
import com.github.phantazmnetwork.neuron.world.Space;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GroundWalker implements Walker {
    private final Space space;
    private final double width;
    private final double height;

    public GroundWalker(@NotNull Space space, double width, double height) {
        this.space = Objects.requireNonNull(space, "space");
        this.width = width;
        this.height = height;
    }

    @Override
    public @NotNull Iterable<Vec3I> walkVectors(int x, int y, int z) {
        double halfWidth = width / 2;

        double minX = x - halfWidth;
        double minY = y;
        double minZ = z - halfWidth;

        double maxX = x + halfWidth;
        double maxY = y + height;
        double maxZ = z + halfWidth;

        //todo: fast way to expand bounds represented by 6 doubles in the direction we are walking

        Iterable<Bounds> collisions = space.collisionsAt(minX, minY, minZ, maxX, maxY, maxZ);
        for(Bounds bounds : collisions) {
            //todo: do detailed collision checking something something complicated code and magic
        }

        //todo: finish this lol
        return null;
    }
}
