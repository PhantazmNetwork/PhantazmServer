package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.ImmutableVec3I;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.node.Node;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * An {@link Explorer} implementation designed for ground-based movement. Agents can walk in any cardinal direction as
 * well as diagonally, and can perform jumps in certain circumstances.
 */
@SuppressWarnings("ClassCanBeRecord")
public class GroundExplorer implements Explorer {
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

    private final WalkingAgent agent;

    /**
     * Creates a new GroundExplorer for the given {@link WalkingAgent}.
     * @param agent the agent this explorer will explore nodes for
     */
    public GroundExplorer(@NotNull WalkingAgent agent) {
        this.agent = Objects.requireNonNull(agent, "agent");
    }

    @Override
    public @NotNull Iterable<? extends Vec3I> walkVectors(@NotNull Node current) {
        int x = current.getX();
        int y = current.getY();
        int z = current.getZ();

        //use a plain Iterator to reduce memory footprint; we don't need to actually store nodes in a collection
        return () -> new Iterator<>() {
            private final Iterator<Vec3I> walkIterator = WALK_VECTORS.iterator();
            private Vec3I next;

            /*
            used to ensure that the iterator shows expected behavior without sacrificing performance. this computes the
            value returned by a call to next() and stores it in a field. if the value could not be computed because the
            iterator has no more elements, this method returns false. otherwise, it returns true

            this is necessary because in order to determine if a next element exists, one must perform the exact same
            calculations used to actually compute the next value.
             */
            private boolean advance() {
                while (walkIterator.hasNext()) {
                    Vec3I walkVector = walkIterator.next();
                    Vec3I next = agent.getCollider().snap(x, y, z, walkVector.getX(), walkVector.getY(),
                            walkVector.getZ(), agent.getJumpHeight(), agent.getFallTolerance());
                    if(next != null && !next.equals(Vec3I.ORIGIN)) {
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
