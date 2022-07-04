package com.github.phantazmnetwork.api.config.processor;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class AttributeMapConfigProcessor implements ConfigProcessor<Object2DoubleMap<String>> {
    @Override
    public @NotNull Object2DoubleMap<String> dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        if (!element.isNode()) {
            throw new ConfigProcessException("element is not a node");
        }

        Object2DoubleMap<String> map = new Object2DoubleOpenHashMap<>(element.asNode().size());
        for (Map.Entry<String, ConfigElement> entry : element.asNode().entrySet()) {
            if (!entry.getValue().isNumber()) {
                throw new ConfigProcessException("value " + entry.getValue() + " is not a number");
            }
            map.put(entry.getKey(), entry.getValue().asNumber().floatValue());
        }

        return map;
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull Object2DoubleMap<String> attributeMap) throws ConfigProcessException {
        ConfigNode node = new LinkedConfigNode(attributeMap.size());
        for (Object2DoubleMap.Entry<String> entry : attributeMap.object2DoubleEntrySet()) {
            node.putNumber(entry.getKey(), entry.getDoubleValue());
        }

        return node;
    }
}
