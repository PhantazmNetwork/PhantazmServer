package com.github.phantazmnetwork.commons.component;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a basic {@link ConfigProcessor} registry which allows the serialization and deserialization of arbitrary
 * data based on {@link Key}.
 */
public interface KeyedConfigRegistry {
    void registerProcessor(@NotNull KeyedConfigProcessor<? extends Keyed> processor);

    boolean hasProcessor(@NotNull Key type);

    @NotNull Keyed deserialize(@NotNull ConfigElement element) throws ConfigProcessException;

    default @NotNull List<Keyed> deserializeList(@NotNull ConfigList list) throws ConfigProcessException {
        List<Keyed> newList = new ArrayList<>(list.size());
        for(ConfigElement element : list) {
            newList.add(deserialize(element));
        }

        return newList;
    }

    @NotNull ConfigElement serialize(Keyed data) throws ConfigProcessException;
}
