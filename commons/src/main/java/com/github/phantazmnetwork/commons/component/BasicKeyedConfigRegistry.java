package com.github.phantazmnetwork.commons.component;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BasicKeyedConfigRegistry implements KeyedConfigRegistry {
    private final Map<Key, KeyedConfigProcessor<? extends Keyed>> processors;

    public BasicKeyedConfigRegistry() {
        this.processors = new HashMap<>();
    }

    @Override
    public void registerProcessor(@NotNull Key key, @NotNull KeyedConfigProcessor<? extends Keyed> processor) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(processor, "processor");

        if (processors.putIfAbsent(key, processor) != null) {
            throw new IllegalArgumentException("Processor for for key " + key + " was already registered");
        }
    }

    @Override
    public boolean hasProcessor(@NotNull Key type) {
        Objects.requireNonNull(type, "type");
        return processors.containsKey(type);
    }

    @Override
    public @NotNull Keyed deserialize(@NotNull ConfigNode node) throws ConfigProcessException {
        Key key = AdventureConfigProcessors.key().dataFromElement(
                node.getElementOrThrow(KeyedConfigProcessor.SERIAL_KEY_NAME));
        KeyedConfigProcessor<? extends Keyed> processor = processors.get(key);
        check(processor, key);

        return processor.dataFromElement(node);
    }

    @Override
    public @NotNull ConfigNode serialize(@NotNull Keyed data) throws ConfigProcessException {
        Key key = data.key();

        //noinspection unchecked
        KeyedConfigProcessor<Keyed> processor = (KeyedConfigProcessor<Keyed>)processors.get(key);
        check(processor, key);

        ConfigElement element = processor.elementFromData(data);
        if (!element.isNode()) {
            throw new ConfigProcessException("Element must be a node");
        }

        return element.asNode();
    }

    @Override
    public @NotNull Key extractKey(@NotNull ConfigNode data) throws ConfigProcessException {
        return AdventureConfigProcessors.key()
                                        .dataFromElement(data.getElementOrThrow(KeyedConfigProcessor.SERIAL_KEY_NAME));
    }

    private static void check(KeyedConfigProcessor<?> obj, Key key) throws ConfigProcessException {
        if (obj == null) {
            throw new ConfigProcessException("No processor found for key " + key);
        }
    }
}
