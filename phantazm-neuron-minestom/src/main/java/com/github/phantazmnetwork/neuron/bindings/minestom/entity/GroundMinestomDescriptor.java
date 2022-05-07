package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.neuron.agent.GroundDescriptor;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link MinestomDescriptor} for gravity-bound entities.
 */
public interface GroundMinestomDescriptor extends MinestomDescriptor, GroundDescriptor {
    @Override
    default float getJumpHeight() {
        return 20F;
    }

    @Override
    default float getFallTolerance() {
        return 16F;
    }

    /**
     * Creates a new GroundMinestomDescriptor using default values, the given {@link EntityType}, and the given id.
     * @param type the EntityType to use
     * @param id the id to use
     * @return a new GroundMinestomDescriptor implementation
     */
    static @NotNull GroundMinestomDescriptor of(@NotNull EntityType type, @NotNull String id) {
        return new GroundMinestomDescriptor() {
            @Override
            public @NotNull EntityType getEntityType() {
                return type;
            }

            @Override
            public @NotNull String getID() {
                return id;
            }
        };
    }
}
