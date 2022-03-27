package com.github.phantazmnetwork.neuron.node;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TranslateCache {
    record Result(boolean isHit, Vec3I getResult) {
        public static final Result MISS = new Result(false, null);
        public static final Result NULL_HIT = new Result(true, null);
    }

    @NotNull Result forAgent(@NotNull Agent agent, int x, int y, int z, int dX, int dY, int dZ);

    void offer(@NotNull Agent agent, int x, int y, int z, int dX, int dY, int dZ, @Nullable Vec3I result);

    void remove(@NotNull Agent agent);

    void clear();

    int size();
}
