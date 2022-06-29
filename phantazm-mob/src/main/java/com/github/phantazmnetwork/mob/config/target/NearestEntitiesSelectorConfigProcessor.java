package com.github.phantazmnetwork.mob.config.target;

import com.github.phantazmnetwork.mob.target.NearestEntitiesSelector;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link ConfigProcessor} for {@link NearestEntitiesSelector}s.
 * @param <TSelector> The type of {@link NearestEntitiesSelector} to process
 */
public abstract class NearestEntitiesSelectorConfigProcessor<TSelector extends NearestEntitiesSelector<?>> implements ConfigProcessor<TSelector> {
    @Override
    public @NotNull TSelector dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        double range = element.getNumberOrThrow("range").doubleValue();
        int targetLimit = element.getNumberOrThrow("targetLimit").intValue();

        return createSelector(range, targetLimit);
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull TSelector selector) {
        ConfigNode element = new LinkedConfigNode(2);

        element.putNumber("range", selector.getRange());
        element.putNumber("targetLimit", selector.getTargetLimit());

        return element;
    }

    /**
     * Creates a {@link TSelector}.
     * @param range The range of the selector
     * @param targetLimit The maximum number of targets to select
     * @return A new {@link TSelector}
     */
    protected abstract @NotNull TSelector createSelector(double range, int targetLimit);

}
