package com.github.phantazmnetwork.zombies.game.map.shop.predicate.logic;

import com.github.phantazmnetwork.commons.config.PrioritizedProcessor;
import com.github.phantazmnetwork.commons.function.TriFunction;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BinaryOperatorProcessor<TData extends BinaryOperatorData> extends PrioritizedProcessor<TData> {
    private final TriFunction<? super Integer, ? super String, ? super String, ? extends TData> dataConstructor;

    public BinaryOperatorProcessor(
            @NotNull TriFunction<? super Integer, ? super String, ? super String, ? extends TData> dataConstructor) {
        this.dataConstructor = Objects.requireNonNull(dataConstructor, "dataConstructor");
    }

    @Override
    public @NotNull TData finishData(@NotNull ConfigNode node, int priority) throws ConfigProcessException {
        String first = node.getStringOrThrow("first");
        String second = node.getStringOrThrow("second");
        return dataConstructor.apply(priority, first, second);
    }

    @Override
    public @NotNull ConfigNode finishNode(@NotNull TData data) {
        ConfigNode node = new LinkedConfigNode(2);
        node.putString("first", data.firstPath());
        node.putString("second", data.secondPath());
        return node;
    }
}
