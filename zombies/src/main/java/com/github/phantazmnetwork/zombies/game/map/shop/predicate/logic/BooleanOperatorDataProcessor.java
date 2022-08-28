package com.github.phantazmnetwork.zombies.game.map.shop.predicate.logic;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class BooleanOperatorDataProcessor<TData extends BooleanOperatorData> implements ConfigProcessor<TData> {
    private static final ConfigProcessor<List<String>> STRING_LIST_PROCESSOR = ConfigProcessor.STRING.listProcessor();

    private final Function<? super List<String>, ? extends TData> dataConstructor;

    public BooleanOperatorDataProcessor(@NotNull Function<? super List<String>, ? extends TData> dataConstructor) {
        this.dataConstructor = Objects.requireNonNull(dataConstructor, "dataConstructor");
    }

    @Override
    public TData dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        List<String> paths = STRING_LIST_PROCESSOR.dataFromElement(element.getElementOrThrow("paths"));
        return dataConstructor.apply(paths);
    }

    @Override
    public @NotNull ConfigElement elementFromData(TData data) throws ConfigProcessException {
        ConfigNode node = new LinkedConfigNode(1);
        node.put("paths", STRING_LIST_PROCESSOR.elementFromData(data.paths()));
        return node;
    }
}
