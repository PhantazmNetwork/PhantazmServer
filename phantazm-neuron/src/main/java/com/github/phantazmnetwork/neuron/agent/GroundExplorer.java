package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.iterator.AdvancingIterator;
import com.github.phantazmnetwork.commons.vector.ImmutableVec3I;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.node.Node;
import com.github.phantazmnetwork.neuron.world.NodeTranslator;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
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
            new ImmutableVec3I(-1, 0, -1)
    );

    private final NodeTranslator translator;

    /**
     * Creates a new GroundExplorer which will use the given {@link NodeTranslator}.
     * @param translator the translator used by this explorer
     */
    public GroundExplorer(@NotNull NodeTranslator translator) {
        this.translator = Objects.requireNonNull(translator, "agent");
    }

    @Override
    public @NotNull Iterable<? extends Vec3I> walkVectors(@NotNull Node current) {
        int x = current.getX();
        int y = current.getY();
        int z = current.getZ();
        Node parent = current.getParent();

        //use AdvancingIterator to reduce memory footprint; we don't need to actually store nodes in a collection
        return () -> new AdvancingIterator<>() {
            private final Iterator<Vec3I> walkIterator = WALK_VECTORS.iterator();

            @Override
            public boolean advance() {
                while (walkIterator.hasNext()) {
                    Vec3I delta = walkIterator.next();

                    /*
                    avoid obvious cases of backtracking with a simple check. although properly-implemented pathfinding
                    algorithms will give the correct path even if we allow backtracking, we should still try to avoid
                    performing collision checks if we don't have to
                     */
                    if(parent == null || !Vec3I.equals(parent.getX(), parent.getY(), parent.getZ(), x + delta.getX(),
                            y + delta.getY(), z + delta.getZ())) {
                        //this might be null, which indicates our walk delta is not traversable
                        Vec3I newDelta = translator.translate(x, y, z, delta.getX(), delta.getY(), delta.getZ());

                        /*
                        only explore the next node if our delta is non-zero (in other words, if we're going to get a new
                        node). PathOperation implementations should handle this case gracefully, but if they don't this
                        may prevent odd behavior
                         */
                        if(newDelta != null && !Vec3I.equals(newDelta.getX(), newDelta.getY(), newDelta.getZ(), 0,
                                0, 0)) {
                            super.value = newDelta;
                            return true;
                        }
                    }
                }

                return false;
            }
        };
    }
}
