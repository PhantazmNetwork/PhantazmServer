package com.github.phantazmnetwork.commons.component;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class BasicKeyedConfigRegistry implements KeyedConfigRegistry {
    private final Map<Key, KeyedConfigProcessor<? extends Keyed>> processors;

    BasicKeyedConfigRegistry() {
        this.processors = new HashMap<>();
    }

    @Override
    public void registerProcessor(@NotNull KeyedConfigProcessor<? extends Keyed> processor) {
        Objects.requireNonNull(processor, "processor");

        if(processors.putIfAbsent(processor.key(), processor) != null) {
            throw new IllegalArgumentException("Processor for for key " + processor.key() + " was already registered");
        }
    }

    @Override
    public boolean hasProcessor(@NotNull Key type) {
        Objects.requireNonNull(type, "type");
        return processors.containsKey(type);
    }

    @Override
    public @NotNull Keyed deserialize(@NotNull ConfigElement element) throws ConfigProcessException {
        try{
            return dataFromElement(element);
        }
        catch (ClassCastException e) {
            throw new ConfigProcessException(e);
        }
    }

    @Override
    public @NotNull ConfigElement serialize(Keyed data) throws ConfigProcessException {
        return elementFromData(data);
    }

    private Keyed dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        if(!element.isNode()) {
            throw new ConfigProcessException("Element is not a node");
        }

        Key key = AdventureConfigProcessors.key().dataFromElement(element.getElementOrThrow("serialKey"));
        KeyedConfigProcessor<? extends Keyed> processor = processors.get(key);
        check(processor, key);

        return processor.dataFromElement(element);
    }

    private @NotNull ConfigElement elementFromData(Keyed keyed) throws ConfigProcessException {
        Key key = keyed.key();

        //noinspection unchecked
        KeyedConfigProcessor<Keyed> processor = (KeyedConfigProcessor<Keyed>) processors.get(key);
        check(processor, key);

        return processor.elementFromData(keyed);
    }

    private static void check(KeyedConfigProcessor<?> obj, Key key) throws ConfigProcessException {
        if(obj == null) {
            throw new ConfigProcessException("No processor found for key " + key);
        }

        if(!obj.key().equals(key)) {
            throw new ConfigProcessException("Cannot process objects of type " + key + " with " + obj.key());
        }
    }
}
