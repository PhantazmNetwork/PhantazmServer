package com.github.phantazmnetwork.mob.config.target;

import com.github.phantazmnetwork.mob.target.NearestEntitySelector;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

public abstract class NearestEntitySelectorConfigProcessor<TSelector extends NearestEntitySelector<?>> implements ConfigProcessor<TSelector> {
    @Override
    public @NotNull TSelector dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        double range = element.getNumberOrThrow("range").doubleValue();
        int targetLimit = element.getNumberOrThrow("targetLimit").intValue();

        return createSelector(range, targetLimit);
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull TSelector selector) {
        ConfigNode element = new LinkedConfigNode();

        element.put("range", new ConfigPrimitive(selector.getRange()));
        element.put("targetLimit", new ConfigPrimitive(selector.getTargetLimit()));

        return element;
    }

    protected abstract @NotNull TSelector createSelector(double range, int targetLimit);

}
