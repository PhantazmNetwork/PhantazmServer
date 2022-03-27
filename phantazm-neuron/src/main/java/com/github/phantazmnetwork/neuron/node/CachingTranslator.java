package com.github.phantazmnetwork.neuron.node;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class CachingTranslator implements NodeTranslator {
    private final EnvironmentCache cache;

    public CachingTranslator(@NotNull EnvironmentCache cache) {
        this.cache = Objects.requireNonNull(cache, "cache");
    }

    @Override
    public @Nullable Vec3I translate(int x, int y, int z, int deltaX, int deltaY, int deltaZ) {
        Agent agent = getAgent();
        if(cache.isCached(agent, x, y, z, deltaX, deltaY, deltaZ)) {
            return cache.forAgent(agent, x, y, z, deltaX, deltaY, deltaZ);
        }

        Vec3I translate = doTranslate(x, y, z, deltaX, deltaY, deltaZ);
        cache.offer(agent, x, y, z, deltaX, deltaY, deltaZ, translate);
        return translate;
    }

    protected abstract @Nullable Vec3I doTranslate(int x, int y, int z, int deltaX, int deltaY, int deltaZ);

    protected abstract @NotNull Agent getAgent();
}
