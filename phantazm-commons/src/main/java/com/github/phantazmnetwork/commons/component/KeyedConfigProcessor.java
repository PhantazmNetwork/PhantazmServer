package com.github.phantazmnetwork.commons.component;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link ConfigProcessor} implementation meant to serialize data which subclasses {@link Keyed}.
 * @param <TData> the Keyed-subclassing data
 */
public abstract class KeyedConfigProcessor<TData extends Keyed> implements ConfigProcessor<TData> {
    /**
     * The name of the key which identifies data and associates it with a particular component.
     */
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

    /**
     * Creates some data from a {@link ConfigNode}.
     * @param node the node to create data from
     * @return the created data
     * @throws ConfigProcessException if an error occurs during configuration processing
     */
    public abstract @NotNull TData dataFromNode(@NotNull ConfigNode node) throws ConfigProcessException;

    /**
     * Creates a {@link ConfigNode} from some data.
     * @param data the data to create a ConfigNode from
     * @return the created ConfigNode
     */
    public abstract @NotNull ConfigNode nodeFromData(@NotNull TData data);
}
