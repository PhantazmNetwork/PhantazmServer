package com.github.phantazmnetwork.neuron.node;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class CachingTranslator implements NodeTranslator {
    private final TranslateCache cache;

    public CachingTranslator(@NotNull TranslateCache cache) {
        this.cache = Objects.requireNonNull(cache, "cache");
    }

    @Override
    public @Nullable Vec3I translate(int x, int y, int z, int deltaX, int deltaY, int deltaZ) {
        Agent.Descriptor agentDescriptor = getDescriptor();
        TranslateCache.Result result = cache.forAgent(agentDescriptor, x, y, z, deltaX, deltaY, deltaZ);
        if(result.isHit()) {
            return result.getResult();
        }

        Vec3I translate = doTranslate(x, y, z, deltaX, deltaY, deltaZ);
        cache.offer(agentDescriptor, x, y, z, deltaX, deltaY, deltaZ, translate);
        return translate;
    }

    protected abstract @Nullable Vec3I doTranslate(int x, int y, int z, int deltaX, int deltaY, int deltaZ);

    protected abstract @NotNull Agent.Descriptor getDescriptor();
}
