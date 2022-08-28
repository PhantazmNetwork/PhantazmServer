package com.github.phantazmnetwork.zombies.game.map.shop.predicate.logic;

import com.github.phantazmnetwork.commons.config.PrioritizedProcessor;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public class BooleanOperatorDataProcessor<TData extends BooleanOperatorData> extends PrioritizedProcessor<TData> {
    private static final ConfigProcessor<List<String>> STRING_LIST_PROCESSOR = ConfigProcessor.STRING.listProcessor();

    private final BiFunction<? super Integer, ? super List<String>, ? extends TData> dataConstructor;

    public BooleanOperatorDataProcessor(
            @NotNull BiFunction<? super Integer, ? super List<String>, ? extends TData> dataConstructor) {
        this.dataConstructor = Objects.requireNonNull(dataConstructor, "dataConstructor");
    }

    @Override
    public @NotNull TData finishData(@NotNull ConfigNode node, int priority) throws ConfigProcessException {
        List<String> paths = STRING_LIST_PROCESSOR.dataFromElement(node.getElementOrThrow("paths"));
        return dataConstructor.apply(priority, paths);
    }

    @Override
    public @NotNull ConfigNode finishNode(@NotNull TData data) throws ConfigProcessException {
        ConfigNode node = new LinkedConfigNode(1);
        node.put("paths", STRING_LIST_PROCESSOR.elementFromData(data.paths()));
        return node;
    }
}
