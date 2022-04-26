package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.neuron.agent.GroundDescriptor;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public interface GroundMinestomDescriptor extends MinestomDescriptor, GroundDescriptor {
    @Override
    default float getJumpHeight() {
        return 1F;
    }

    @Override
    default float getFallTolerance() {
        return 16F;
    }

    default float getStep() {
        return 0.5F;
    }

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
