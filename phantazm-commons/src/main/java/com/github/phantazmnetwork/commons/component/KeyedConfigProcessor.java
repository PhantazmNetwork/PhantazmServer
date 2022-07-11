package com.github.phantazmnetwork.commons.component;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public abstract class KeyedConfigProcessor<TData extends Keyed> implements ConfigProcessor<TData>, Keyed {
    public static final String SERIAL_KEY_NAME = "serialKey";

    @Override
    public final TData dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        if(!element.isNode()) {
            throw new ConfigProcessException("Element must be a node");
        }

        Key key = AdventureConfigProcessors.key().dataFromElement(element.getElementOrThrow(SERIAL_KEY_NAME));
        if(!key.equals(key())) {
            throw new ConfigProcessException("Cannot deserialize serial elements of type " + key);
        }

        return dataFromNode(element.asNode());
    }

    @Override
    public final @NotNull ConfigElement elementFromData(TData data) throws ConfigProcessException {
        if(!data.key().equals(key())) {
            throw new ConfigProcessException("Cannot serialize data with key of type " + data.key());
        }

        ConfigNode dataNode = nodeFromData(data);
        ConfigNode newNode = new LinkedConfigNode(dataNode.size() + 1);
        newNode.put(SERIAL_KEY_NAME, AdventureConfigProcessors.key().elementFromData(key()));
        newNode.putAll(dataNode);

        return newNode;
    }

    public abstract TData dataFromNode(@NotNull ConfigNode node) throws ConfigProcessException;

    public abstract @NotNull ConfigNode nodeFromData(TData data);
}
