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
    @Override
    public final TData dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        if(!element.isNode()) {
            throw new ConfigProcessException("Element must be a node");
        }

        Key key = AdventureConfigProcessors.key().dataFromElement(element.getElementOrThrow("serialKey"));
        if(!key.equals(key())) {
            throw new ConfigProcessException("Cannot deserialize serial elements of type " + key);
        }

        return dataFromElementKey(element.asNode());
    }

    @Override
    public final @NotNull ConfigElement elementFromData(TData data) throws ConfigProcessException {
        if(!data.key().equals(key())) {
            throw new ConfigProcessException("Cannot serialize data with key of type " + data.key());
        }

        ConfigNode dataNode = elementFromDataKey(data);
        ConfigNode newNode = new LinkedConfigNode(dataNode.size() + 1);
        newNode.put("serialKey", AdventureConfigProcessors.key().elementFromData(key()));
        newNode.putAll(dataNode);

        return newNode;
    }

    public abstract TData dataFromElementKey(@NotNull ConfigNode node) throws ConfigProcessException;

    public abstract @NotNull ConfigNode elementFromDataKey(TData data);
}
