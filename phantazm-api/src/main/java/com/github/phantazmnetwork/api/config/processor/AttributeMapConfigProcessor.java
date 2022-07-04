package com.github.phantazmnetwork.api.config.processor;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class AttributeMapConfigProcessor {

    public AttributeMapConfigProcessor() {
        throw new UnsupportedOperationException();
    }

    private static final ConfigProcessor<Object2FloatMap<String>> PROCESSOR = new ConfigProcessor<>() {
        @Override
        public @NotNull Object2FloatMap<String> dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            if (!element.isNode()) {
                throw new ConfigProcessException("element is not a node");
            }

            Object2FloatMap<String> map = new Object2FloatOpenHashMap<>(element.asNode().size());
            for (Map.Entry<String, ConfigElement> entry : element.asNode().entrySet()) {
                if (!entry.getValue().isNumber()) {
                    throw new ConfigProcessException("value " + entry.getValue() + " is not a number");
                }
                map.put(entry.getKey(), entry.getValue().asNumber().floatValue());
            }

            return map;
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull Object2FloatMap<String> attributeMap) {
            ConfigNode node = new LinkedConfigNode(attributeMap.size());
            for (Object2FloatMap.Entry<String> entry : attributeMap.object2FloatEntrySet()) {
                node.putNumber(entry.getKey(), entry.getFloatValue());
            }

            return node;
        }
    };

    public static @NotNull ConfigProcessor<Object2FloatMap<String>> processor() {
        return PROCESSOR;
    }


}
