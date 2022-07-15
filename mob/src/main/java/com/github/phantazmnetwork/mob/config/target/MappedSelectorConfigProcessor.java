package com.github.phantazmnetwork.mob.config.target;

import com.github.phantazmnetwork.mob.target.MappedSelector;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link ConfigProcessor} for {@link MappedSelector}s.
 *
 * @param <TFrom>     The target type of the delegate {@link MappedSelector}
 * @param <TSelector> The type of {@link MappedSelector} to process
 */
public abstract class MappedSelectorConfigProcessor<TFrom, TSelector extends MappedSelector<TFrom, ?>>
        implements ConfigProcessor<TSelector> {

    private final ConfigProcessor<? extends TargetSelector<TFrom>> delegateProcessor;

    /**
     * Creates a {@link MappedSelectorConfigProcessor}.
     *
     * @param delegateProcessor A {@link ConfigProcessor} for the delegate {@link TargetSelector}
     */
    public MappedSelectorConfigProcessor(@NotNull ConfigProcessor<? extends TargetSelector<TFrom>> delegateProcessor) {
        this.delegateProcessor = Objects.requireNonNull(delegateProcessor, "delegateProcessor");
    }

    @Override
    public TSelector dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        return createSelector(delegateProcessor.dataFromElement(element));
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull ConfigElement elementFromData(@NotNull TSelector selector) throws ConfigProcessException {
        ConfigProcessor<TargetSelector<TFrom>> genericProcessor =
                (ConfigProcessor<TargetSelector<TFrom>>)delegateProcessor;
        return genericProcessor.elementFromData(selector.getDelegate());
    }

    /**
     * Creates a new {@link TSelector}.
     *
     * @param delegate The delegate {@link TargetSelector}
     * @return A new {@link TSelector}
     */
    protected abstract @NotNull TSelector createSelector(@NotNull TargetSelector<TFrom> delegate);

}
