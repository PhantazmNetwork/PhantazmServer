package com.github.phantazmnetwork.commons.config;

import com.github.phantazmnetwork.commons.Prioritized;
import com.github.phantazmnetwork.commons.component.KeyedConfigProcessor;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

/**
 * {@link KeyedConfigProcessor} implementation designed for serializing/deserializing {@link Prioritized} objects.
 *
 * @param <TData> the type of data to process
 */
public abstract class PrioritizedProcessor<TData extends Prioritized & Keyed> extends KeyedConfigProcessor<TData> {
    @Override
    public final @NotNull TData dataFromNode(@NotNull ConfigNode node) throws ConfigProcessException {
        int priority = node.getNumberOrThrow("priority").intValue();
        return finishData(node, priority);
    }

    @Override
    public final @NotNull ConfigNode nodeFromData(@NotNull TData data) throws ConfigProcessException {
        ConfigNode node = finishNode(data);

        ConfigNode newNode = new LinkedConfigNode(node.size() + 1);
        newNode.putNumber("priority", data.priority());
        newNode.putAll(node);

        return newNode;
    }

    public abstract @NotNull TData finishData(@NotNull ConfigNode node, int priority) throws ConfigProcessException;

    public abstract @NotNull ConfigNode finishNode(@NotNull TData data) throws ConfigProcessException;
}
