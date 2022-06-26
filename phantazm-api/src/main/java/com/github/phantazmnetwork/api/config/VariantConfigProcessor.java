package com.github.phantazmnetwork.api.config;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class VariantConfigProcessor<TValue> implements ConfigProcessor<TValue> {

    private final Map<Key, ConfigProcessor<? extends TValue>> subProcessors;

    private final ConfigProcessor<Key> keyProcessor;

    public VariantConfigProcessor(@NotNull Map<Key, ConfigProcessor<? extends TValue>> subProcessors,
                                  @NotNull ConfigProcessor<Key> keyProcessor) {
        this.subProcessors = Objects.requireNonNull(subProcessors, "subProcessors");
        this.keyProcessor = Objects.requireNonNull(keyProcessor, "keyProcessor");
    }

    @Override
    public @NotNull TValue dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        ConfigElement serialKeyElement = element.getElementOrThrow("serialKey");
        Key key = keyProcessor.dataFromElement(serialKeyElement);
        ConfigProcessor<? extends TValue> processor = subProcessors.get(key);
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
        ConfigProcessor<TValue> processor = (ConfigProcessor<TValue>) subProcessors.get(serializable.getSerialKey());
        if (processor == null) {
            throw new ConfigProcessException("no subprocessor");
        }

        ConfigElement element = processor.elementFromData(data);
        if (!(element instanceof ConfigNode node)) {
            throw new ConfigProcessException("subprocessor must return ConfigNode");
        }
        node.put("serialKey", keyProcessor.elementFromData(serializable.getSerialKey()));

        return node;
    }
}
