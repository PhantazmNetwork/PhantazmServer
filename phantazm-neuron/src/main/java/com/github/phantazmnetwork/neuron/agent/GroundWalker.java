package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.ImmutableVec3I;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.world.Collider;
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
            new ImmutableVec3I(-1, 0, -1)
    );

    private static final Vec3I JUMP_VECTOR = new ImmutableVec3I(0, 1, 0);

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
                Collider collider = agent.getCollider();
                while (walkIterator.hasNext()) {
                    Vec3I walkVector = walkIterator.next();

                    int nX = x + walkVector.getX();
                    int nY = y + walkVector.getY();
                    int nZ = z + walkVector.getZ();

                    float finalY = collider.collidesMovingAlong(x, y, z, walkVector.getX(), walkVector.getY(),
                            walkVector.getZ()) ? collider.findHighest(nX, nY, nZ, agent.getJumpHeight()) : collider
                            .findLowest(nX, nY, nZ, agent.getFallTolerance());

                    //infinite values indicate the collision check short-circuited because the limit was exceeded
                    if(!Float.isInfinite(finalY)) {
                        next = new ImmutableVec3I(nX, (int)finalY, nZ);
                        return true;
                    }
                }

                //potentially jump to escape things we're stuck in
                if(!collider.collidesMovingAlong(x, y, z, JUMP_VECTOR.getX(), JUMP_VECTOR.getY(), JUMP_VECTOR.getZ())) {
                    float finalY = collider.findHighest(x, y, z, agent.getJumpHeight());
                    if(!Float.isInfinite(finalY)) {
                        int finalNodeY = (int)finalY;
                        if(finalNodeY > y && !collider.collidesAt(x, finalNodeY, z)) {
                            next = new ImmutableVec3I(x, finalNodeY, z);
                            return true;
                        }
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
