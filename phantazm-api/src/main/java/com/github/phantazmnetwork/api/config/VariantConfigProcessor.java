package com.github.phantazmnetwork.api.config;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class VariantConfigProcessor<TValue> implements ConfigProcessor<TValue> {

    private final Map<String, ConfigProcessor<TValue>> subProcessors;

    public VariantConfigProcessor(@NotNull Map<String, ConfigProcessor<TValue>> subProcessors) {
        this.subProcessors = Objects.requireNonNull(subProcessors, "subProcessors");
    }

    @Override
    public @NotNull TValue dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        String type = element.getStringOrDefault("type");
        ConfigProcessor<TValue> processor = subProcessors.get(type);
        if (processor == null) {
            throw new ConfigProcessException("no subprocessor");
        }

        return processor.dataFromElement(element.getElementOrThrow("skill"));
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull TValue skill) {
        return null;
    }
}
