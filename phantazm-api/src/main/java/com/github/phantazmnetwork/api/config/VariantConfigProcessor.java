package com.github.phantazmnetwork.api.config;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

/**
 * A {@link ConfigProcessor} which processes {@link TValue}s.
 * This can be used to process inheritance and identifies variants based on {@link Keyed#key()}.
 * @param <TValue> The type of {@link TValue} to process
 */
public class VariantConfigProcessor<TValue extends Keyed> implements ConfigProcessor<TValue> {

    private static final ConfigProcessor<Key> KEY_PROCESSOR = AdventureConfigProcessors.key();

    private final Function<Key, ConfigProcessor<? extends TValue>> subProcessors;

    /**
     * Creates a new {@link VariantConfigProcessor}.
     * @param subprocessorProvider A {@link Function} that provides {@link ConfigProcessor} based on a {@link Key},
     *                             or null if no such {@link ConfigProcessor} is available
     */
    public VariantConfigProcessor(@NotNull Function<Key, ConfigProcessor<? extends TValue>> subprocessorProvider) {
        this.subProcessors = Objects.requireNonNull(subprocessorProvider, "subProcessors");
    }

    @Override
    public @NotNull TValue dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        ConfigElement serialKeyElement = element.getElementOrThrow("serialKey");
        Key key = KEY_PROCESSOR.dataFromElement(serialKeyElement);
        ConfigProcessor<? extends TValue> processor = subProcessors.apply(key);
        if (processor == null) {
            throw new ConfigProcessException("no subprocessor");
        }

        return processor.dataFromElement(element.getElementOrThrow("data"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull ConfigElement elementFromData(@NotNull TValue data) throws ConfigProcessException {
        ConfigProcessor<TValue> processor = (ConfigProcessor<TValue>) subProcessors.apply(data.key());
        if (processor == null) {
            throw new ConfigProcessException("no subprocessor");
        }

        ConfigNode node = new LinkedConfigNode(2);
        node.put("serialKey", KEY_PROCESSOR.elementFromData(data.key()));
        node.put("data", processor.elementFromData(data));

        return node;
    }
}
