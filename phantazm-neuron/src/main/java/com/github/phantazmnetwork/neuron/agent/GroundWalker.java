package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.ImmutableVec3I;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.world.TerrainCollider;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class GroundWalker implements Walker {
    private static final Iterable<Vec3I> WALK_VECTORS = List.of(
            new ImmutableVec3I(1, 0, 0),
            new ImmutableVec3I(-1, 0, 0),
            new ImmutableVec3I(0, 0, 1),
            new ImmutableVec3I(0, 0, -1),
            new ImmutableVec3I(1, 0, 1),
            new ImmutableVec3I(1, 0, -1),
            new ImmutableVec3I(-1, 0, 1),
            new ImmutableVec3I(-1, 0, -1),
            new ImmutableVec3I(0, 1, 0)
    );

    private final GroundAgent agent;

    public GroundWalker(@NotNull GroundAgent agent) {
        this.agent = Objects.requireNonNull(agent, "agent");
    }

    @Override
    public @NotNull Iterable<? extends Vec3I> walkVectors(int x, int y, int z) {
        return () -> new Iterator<>() {
            private final Iterator<Vec3I> walkIterator = WALK_VECTORS.iterator();
            private Vec3I next;

            private boolean advance() {
                TerrainCollider terrainCollider = agent.getTerrainCollider();
                while (walkIterator.hasNext()) {
                    Vec3I walkVector = walkIterator.next();
                    Vec3I next = terrainCollider.snap(x, y, z, walkVector.getX(), walkVector.getY(), walkVector.getZ());
                    if(next != null) {
                        this.next = next;
                        return true;
                    }
                }

                return false;
            }

            @Override
            public boolean hasNext() {
                if(next != null) {
                    return true;
                }

                return advance();
            }

            @Override
            public Vec3I next() {
                if(this.next == null && !advance()) {
                    throw new NoSuchElementException();
                }

                Vec3I next = this.next;
                this.next = null;
                return next;
            }
        };
    }
}
