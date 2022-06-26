package com.github.phantazmnetwork.api.config;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class VariantConfigProcessor<TValue> implements ConfigProcessor<TValue> {

    private final Map<String, ConfigProcessor<? extends TValue>> subProcessors;

    public VariantConfigProcessor(@NotNull Map<String, ConfigProcessor<? extends TValue>> subProcessors) {
        this.subProcessors = Objects.requireNonNull(subProcessors, "subProcessors");
    }

    @Override
    public @NotNull TValue dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        String serialName = element.getStringOrDefault("serialName");
        ConfigProcessor<? extends TValue> processor = subProcessors.get(serialName);
        if (processor == null) {
            throw new ConfigProcessException("no subprocessor");
        }

        return processor.dataFromElement(element);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull ConfigElement elementFromData(@NotNull TValue data) throws ConfigProcessException {
        if (!(data instanceof VariantSerializable serializable)) {
            throw new ConfigProcessException("data not variant serializable");
        }
        ConfigProcessor<TValue> processor = (ConfigProcessor<TValue>) subProcessors.get(serializable.getSerialName());
        if (processor == null) {
            throw new ConfigProcessException("no subprocessor");
        }

        return processor.elementFromData(data);
    }
}
