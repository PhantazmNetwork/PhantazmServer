package com.github.phantazmnetwork.mob.config.target;

import com.github.phantazmnetwork.mob.target.MappedSelector;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class MappedSelectorConfigProcessor<TFrom, TSelector extends MappedSelector<TFrom, ?>> implements ConfigProcessor<TSelector> {

    private final ConfigProcessor<? extends TargetSelector<TFrom>> delegateProcessor;

    public MappedSelectorConfigProcessor(@NotNull ConfigProcessor<? extends TargetSelector<TFrom>> delegateProcessor) {
        this.delegateProcessor = Objects.requireNonNull(delegateProcessor, "delegateProcessor");
    }

    @Override
    public TSelector dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        return createSelector(delegateProcessor.dataFromElement(element));
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull TSelector selector) throws ConfigProcessException {
        ConfigProcessor<TargetSelector<TFrom>> genericProcessor = (ConfigProcessor<TargetSelector<TFrom>>) delegateProcessor;
        return genericProcessor.elementFromData(selector.getDelegate());
    }

    protected abstract @NotNull TSelector createSelector(@NotNull TargetSelector<TFrom> delegate);

}
