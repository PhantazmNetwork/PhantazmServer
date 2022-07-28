package com.github.phantazmnetwork.mob.config.target;

import com.github.phantazmnetwork.mob.target.SelfSelector;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link ConfigProcessor} for {@link SelfSelector}s.
 */
public class SelfSelectorConfigProcessor implements ConfigProcessor<SelfSelector> {
    @Override
    public @NotNull SelfSelector dataFromElement(@NotNull ConfigElement element) {
        return new SelfSelector();
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull SelfSelector selfSelector) {
        return new LinkedConfigNode(0);
    }
}
