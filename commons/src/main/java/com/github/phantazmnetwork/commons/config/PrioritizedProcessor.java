package com.github.phantazmnetwork.commons.config;

import com.github.phantazmnetwork.commons.Prioritized;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

/**
 * {@link ConfigProcessor} implementation designed for serializing/deserializing {@link Prioritized} objects.
 *
 * @param <TData> the type of data to process
 */
public abstract class PrioritizedProcessor<TData extends Prioritized> implements ConfigProcessor<TData> {
    @Override
    public final @NotNull TData dataFromElement(@NotNull ConfigElement node) throws ConfigProcessException {
        int priority = node.getNumberOrThrow("priority").intValue();
        return finishData(node.asNode(), priority);
    }

    @Override
    public final @NotNull ConfigNode elementFromData(@NotNull TData data) throws ConfigProcessException {
        ConfigNode node = finishNode(data);

        ConfigNode newNode = new LinkedConfigNode(node.size() + 1);
        newNode.putNumber("priority", data.priority());
        newNode.putAll(node);

        return newNode;
    }

    public abstract @NotNull TData finishData(@NotNull ConfigNode node, int priority) throws ConfigProcessException;

    public abstract @NotNull ConfigNode finishNode(@NotNull TData data) throws ConfigProcessException;
}
