package com.github.phantazmnetwork.api.config;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SoundConfigProcessor implements ConfigProcessor<Sound> {

    private final ConfigProcessor<Key> keyConfigProcessor;

    public SoundConfigProcessor(@NotNull ConfigProcessor<Key> keyConfigProcessor) {
        this.keyConfigProcessor = Objects.requireNonNull(keyConfigProcessor, "keyConfigProcessor");
    }

    @Override
    public Sound dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        Key name = keyConfigProcessor.dataFromElement(element.getElement("name"));
        Sound.Source source = Sound.Source.NAMES.value(element.getStringOrDefault("source").toUpperCase());
        if (source == null) {
            throw new ConfigProcessException("unknown source");
        }
        float volume = element.getNumberOrThrow("volume").floatValue();
        float pitch = element.getNumberOrThrow("pitch").floatValue();

        return Sound.sound(name, source, volume, pitch);
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull Sound sound) throws ConfigProcessException {
        ConfigNode node = new LinkedConfigNode();
        node.put("name", keyConfigProcessor.elementFromData(sound.name()));
        node.put("source", new ConfigPrimitive(sound.source().name().toUpperCase()));
        node.put("volume", new ConfigPrimitive(sound.volume()));
        node.put("pitch", new ConfigPrimitive(sound.pitch()));

        return node;
    }
}
