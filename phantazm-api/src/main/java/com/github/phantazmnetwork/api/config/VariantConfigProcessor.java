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

import java.util.Map;
import java.util.Objects;

public class VariantConfigProcessor<TValue extends Keyed> implements ConfigProcessor<TValue> {

    private static final ConfigProcessor<Key> KEY_PROCESSOR = AdventureConfigProcessors.key();

    private final Map<Key, ConfigProcessor<? extends TValue>> subProcessors;

    public VariantConfigProcessor(@NotNull Map<Key, ConfigProcessor<? extends TValue>> subProcessors) {
        this.subProcessors = Objects.requireNonNull(subProcessors, "subProcessors");
    }

    @Override
    public @NotNull TValue dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        ConfigElement serialKeyElement = element.getElementOrThrow("serialKey");
        Key key = KEY_PROCESSOR.dataFromElement(serialKeyElement);
        ConfigProcessor<? extends TValue> processor = subProcessors.get(key);
        if (processor == null) {
            throw new ConfigProcessException("no subprocessor");
        }

        return processor.dataFromElement(element.getElementOrThrow("data"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull ConfigElement elementFromData(@NotNull TValue data) throws ConfigProcessException {
        ConfigProcessor<TValue> processor = (ConfigProcessor<TValue>) subProcessors.get(data.key());
        if (processor == null) {
            throw new ConfigProcessException("no subprocessor");
        }

        ConfigNode node = new LinkedConfigNode(2);
        node.put("serialKey", KEY_PROCESSOR.elementFromData(data.key()));
        node.put("data", processor.elementFromData(data));

        return node;
    }
}
