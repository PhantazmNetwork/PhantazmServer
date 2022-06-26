package com.github.phantazmnetwork.commons;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ArrayConfigList;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ConfigProcessorUtils {
    private ConfigProcessorUtils() {
        throw new UnsupportedOperationException();
    }

    public static <TType> @NotNull ConfigProcessor<List<TType>> newListProcessor(
            @NotNull ConfigProcessor<TType> elementProcessor) {
        return new ConfigProcessor<>() {
            @Override
            public List<TType> dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                if(!element.isList()) {
                    throw new ConfigProcessException("Element must be a list");
                }

                ConfigList list = element.asList();
                List<TType> elements = new ArrayList<>(list.size());
                for(ConfigElement object : list) {
                    elements.add(elementProcessor.dataFromElement(object));
                }

                return elements;
            }

            @Override
            public @NotNull ConfigElement elementFromData(List<TType> elements) throws ConfigProcessException {
                ConfigList list = new ArrayConfigList(elements.size());
                for(TType element : elements) {
                    list.add(elementProcessor.elementFromData(element));
                }

                return list;
            }
        };
    }

    public static <TType extends Enum<?>> @NotNull ConfigProcessor<TType> newEnumProcessor(
            @NotNull Class<TType> enumClass) {
        TType[] constants = enumClass.getEnumConstants();

        return new ConfigProcessor<>() {
            @Override
            public TType dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                if(!element.isString()) {
                    throw new ConfigProcessException("Element must be a string");
                }

                String elementString = element.asString();
                for(TType constant : constants) {
                    if(constant.toString().equals(elementString)) {
                        return constant;
                    }
                }

                throw new ConfigProcessException("String " + elementString + " cannot be converted to an enum of type "
                        + enumClass.getTypeName());
            }

            @Override
            public @NotNull ConfigElement elementFromData(TType tType) throws ConfigProcessException {
                if(tType == null) {
                    throw new ConfigProcessException("Cannot convert null type to a ConfigElement");
                }

                return new ConfigPrimitive(tType.toString());
            }
        };
    }
}
