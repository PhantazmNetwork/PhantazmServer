package com.github.phantazmnetwork.commons.component;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public abstract class KeyedConfigProcessor<TData extends Keyed> implements ConfigProcessor<TData> {
    public static final String SERIAL_KEY_NAME = "serialKey";

    @Override
    public final TData dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        if(!element.isNode()) {
            throw new ConfigProcessException("Element must be a node");
        }

        return dataFromNode(element.asNode());
    }

    @Override
    public final @NotNull ConfigElement elementFromData(TData data) throws ConfigProcessException {
        ConfigNode dataNode = nodeFromData(data);
        ConfigNode newNode = new LinkedConfigNode(dataNode.size() + 1);
        newNode.put(SERIAL_KEY_NAME, AdventureConfigProcessors.key().elementFromData(data.key()));
        newNode.putAll(dataNode);

        return newNode;
    }

    public abstract @NotNull TData dataFromNode(@NotNull ConfigNode node) throws ConfigProcessException;

    public abstract @NotNull ConfigNode nodeFromData(@NotNull TData data);
}
