package com.github.phantazmnetwork.neuron.bindings.minestom.chunk;

import com.github.phantazmnetwork.api.PhysicsUtils;
import com.github.phantazmnetwork.neuron.bindings.minestom.solid.SolidProvider;
import com.github.phantazmnetwork.neuron.world.Solid;
import net.minestom.server.collision.Shape;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A custom extension of {@link DynamicChunk} that enables fast, asynchronous access to {@link Solid} objects
 * representing collision bounds.
 */
@SuppressWarnings("UnstableApiUsage")
public class NeuralChunk extends DynamicChunk {
    /**
     * Creates a new NeuralChunk for the given instance.
     * @param instance the instance this chunk belongs to
     * @param chunkX the chunk's x-coordinate
     * @param chunkZ the chunk's z-coordinate
     */
    public NeuralChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
        super(instance, chunkX, chunkZ);
    }

    /**
     * Tries to obtain a {@link Solid} located at the provided world coordinates. This properly accounts for blocks
     * larger than 1, like walls and fences.
     * @param x the x-coordinate of the solid
     * @param y the y-coordinate of the solid
     * @param z the z-coordinate of the solid
     * @return the Solid located at the provided coordinates, or null if none exists
     */
    public @Nullable Solid getSolid(int x, int y, int z) {
        Block current = getBlock_UNSAFE(x, y, z, Condition.TYPE);
        Shape currentShape = current.registry().collisionShape();

        double height = currentShape.relativeEnd().y();
        if(height > 0.5) { //no point in checking below
            return SolidProvider.fromShape(currentShape);
        }

        //we need to check below
        Block below = getBlock_UNSAFE(x, y - 1, z, Condition.TYPE);
        Shape belowShape = below.registry().collisionShape();

        boolean currentCollidable = PhysicsUtils.isCollidable(currentShape);

        if(PhysicsUtils.isTall(belowShape)) {
            //split tall shape
            Solid upperSolid = SolidProvider.getSplit(belowShape)[1];

            if(currentCollidable) {
                //if the current shape also has collision, combine with the split
                return SolidProvider.composite(upperSolid, SolidProvider.fromShape(currentShape));
            }

            return upperSolid;
        }

        if(!currentCollidable) {
            return null;
        }

        return SolidProvider.fromShape(currentShape);
    }
}
