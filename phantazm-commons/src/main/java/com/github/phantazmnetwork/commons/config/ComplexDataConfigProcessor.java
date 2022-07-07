package com.github.phantazmnetwork.commons.config;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ComplexDataConfigProcessor implements ConfigProcessor<ComplexData> {

    private static final ConfigProcessor<Key> KEY_PROCESSOR = AdventureConfigProcessors.key();

    private final Map<Key, ConfigProcessor<? extends Keyed>> subProcessors;

    public ComplexDataConfigProcessor(@NotNull Map<Key, ConfigProcessor<? extends Keyed>> subProcessors) {
        this.subProcessors = Objects.requireNonNull(subProcessors, "subprocessors");
    }

    @Override
    public @NotNull ComplexData dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        if (!element.isNode()) {
            throw new ConfigProcessException("Config element must be a node");
        }

        Map<Key, Keyed> objects = new HashMap<>();
        Key mainKey = KEY_PROCESSOR.dataFromElement(element.getElementOrThrow("mainKey"));
        for (Map.Entry<String, ConfigElement> dependencyElement : element.asNode().entrySet()) {
            ConfigElement value = dependencyElement.getValue();
            if (!value.isNode()) {
                continue;
            }

            ConfigNode node = value.asNode();
            Key name = KEY_PROCESSOR.dataFromElement(new ConfigPrimitive(dependencyElement.getKey()));
            Key serialKey = KEY_PROCESSOR.dataFromElement(value.getElementOrThrow("serialKey"));

            ConfigProcessor<? extends Keyed> subprocessor = subProcessors.get(serialKey);
            if (subprocessor == null) {
                throw new ConfigProcessException("No subprocessor found for key " + serialKey);
            }

            Keyed object = subprocessor.dataFromElement(node);
            objects.put(name, object);
        }

        return new ComplexData(mainKey, objects);
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull ComplexData data) throws ConfigProcessException {
        ConfigNode node = new LinkedConfigNode(data.objects().size() + 1);
        node.put("mainKey", KEY_PROCESSOR.elementFromData(data.mainKey()));

        for (Map.Entry<Key, Keyed> entry : data.objects().entrySet()) {
            ConfigElement name = KEY_PROCESSOR.elementFromData(entry.getKey());
            if (!name.isString()) {
                throw new ConfigProcessException("Key processor must return a string");
            }

            Keyed object = entry.getValue();
            Key objectKey = object.key();

            ConfigProcessor<? extends Keyed> processor = subProcessors.get(objectKey);
            if (processor == null) {
                throw new ConfigProcessException("No subprocessor found for key " + objectKey);
            }

            ConfigElement element = dependencyElementFromData(processor, entry.getValue());
            if (!element.isNode()) {
                throw new ConfigProcessException("Subprocessor must return a node");
            }

            ConfigNode dependencyNode = new LinkedConfigNode(element.asNode().size() + 1);
            dependencyNode.put("serialKey", KEY_PROCESSOR.elementFromData(objectKey));
            dependencyNode.putAll(element.asNode());

            node.put(name.asString(), dependencyNode);
        }

        return node;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Keyed> @NotNull ConfigElement dependencyElementFromData(@NotNull ConfigProcessor<T> configProcessor,
                                                                                      @NotNull Keyed data) throws ConfigProcessException {
        try {
            return configProcessor.elementFromData((T) data);
        }
        catch (ClassCastException e) {
            throw new ConfigProcessException("Mismatched data type for serial key" + data.key(), e);
        }
    }

}
